import React, { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Image,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { router, Stack, useFocusEffect } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { wishlistApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Card } from '../../../components/Card';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { WishlistItemResponse, WishlistPriority } from '../../../types/api';

const PRIORITY_COLOR: Record<WishlistPriority, string> = {
  LOW: '#9CA3AF',
  MEDIUM: '#F59E0B',
  HIGH: '#EF4444',
};

type Filter = 'all' | 'active' | 'purchased';

export default function WishlistScreen() {
  const colors = useThemeColors();
  const [items, setItems] = useState<WishlistItemResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState<Filter>('all');

  const load = useCallback(() => {
    setLoading(true);
    wishlistApi.list().then((r) => {
      if (r.ok) setItems(r.data);
      else setError('Could not load wishlist.');
      setLoading(false);
    });
  }, []);

  useFocusEffect(load);

  const filtered =
    filter === 'active'
      ? items.filter((i) => !i.isPurchased)
      : filter === 'purchased'
      ? items.filter((i) => i.isPurchased)
      : items;

  return (
    <Screen scroll={false} padded={false}>
      <Stack.Screen
        options={{
          title: 'Wishlist',
          headerRight: () => (
            <Pressable
              onPress={() => router.push('/(app)/wishlist/new')}
              style={({ pressed }) => [styles.addBtn, pressed && styles.pressed]}
              accessibilityLabel="Add to wishlist"
            >
              <Ionicons name="add" size={28} color={colors.primary} />
            </Pressable>
          ),
        }}
      />

      <View style={[styles.filters, { borderBottomColor: colors.border }]}>
        {(['all', 'active', 'purchased'] as Filter[]).map((f) => (
          <Pressable
            key={f}
            onPress={() => setFilter(f)}
            style={[styles.chip, filter === f && { backgroundColor: colors.primary }]}
          >
            <Text style={[styles.chipText, { color: filter === f ? '#fff' : colors.textSecondary }]}>
              {f.charAt(0).toUpperCase() + f.slice(1)}
            </Text>
          </Pressable>
        ))}
      </View>

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : error ? (
        <View style={styles.center}>
          <Text style={{ color: colors.danger }}>{error}</Text>
        </View>
      ) : filtered.length === 0 ? (
        <View style={styles.center}>
          <Ionicons name="heart-outline" size={48} color={colors.textSecondary} />
          <Text style={[styles.emptyText, { color: colors.textSecondary }]}>
            {filter === 'all' ? 'Your wishlist is empty' : `No ${filter} items`}
          </Text>
        </View>
      ) : (
        <FlatList
          data={filtered}
          keyExtractor={(i) => i.id}
          contentContainerStyle={styles.list}
          renderItem={({ item }) => (
            <Pressable onPress={() => router.push(`/(app)/wishlist/${item.id}`)}>
              <Card style={[styles.card, item.isPurchased && styles.purchasedCard]}>
                <ProductThumbnail uri={item.imageUrl} />
                <View style={styles.cardContent}>
                  <View style={styles.cardRow}>
                    <View style={[styles.dot, { backgroundColor: PRIORITY_COLOR[item.priority] }]} />
                    <Text
                      style={[styles.cardTitle, { color: colors.text }, item.isPurchased && styles.strikethrough]}
                      numberOfLines={2}
                    >
                      {item.name}
                    </Text>
                    {item.isPurchased && (
                      <Ionicons name="checkmark-circle" size={18} color={colors.success} style={styles.checkIcon} />
                    )}
                  </View>
                  {item.notes ? (
                    <Text style={[styles.notes, { color: colors.textSecondary }]} numberOfLines={1}>
                      {item.notes}
                    </Text>
                  ) : null}
                  <Text style={[styles.priority, { color: PRIORITY_COLOR[item.priority] }]}>
                    {item.priority}
                  </Text>
                </View>
              </Card>
            </Pressable>
          )}
        />
      )}
    </Screen>
  );
}

function ProductThumbnail({ uri }: { uri: string | null }) {
  const colors = useThemeColors();
  const [errored, setErrored] = useState(false);

  if (uri && !errored) {
    return (
      <Image
        source={{ uri }}
        style={styles.thumbnail}
        resizeMode="cover"
        onError={() => setErrored(true)}
      />
    );
  }
  return (
    <View style={[styles.thumbnail, styles.thumbnailPlaceholder, { backgroundColor: colors.border }]}>
      <Ionicons name="bag-outline" size={28} color={colors.textSecondary} />
    </View>
  );
}

const styles = StyleSheet.create({
  filters: {
    flexDirection: 'row',
    gap: spacing.xs,
    padding: spacing.md,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  chip: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#D1D5DB',
  },
  chipText: { fontSize: typography.fontSize.xs },
  list: { padding: spacing.md, gap: spacing.sm },
  card: { padding: spacing.sm, flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  purchasedCard: { opacity: 0.55 },
  thumbnail: { width: 70, height: 70, borderRadius: 10 },
  thumbnailPlaceholder: { alignItems: 'center', justifyContent: 'center' },
  cardContent: { flex: 1, gap: spacing.xs },
  cardRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  dot: { width: 8, height: 8, borderRadius: 4, flexShrink: 0 },
  cardTitle: { flex: 1, fontSize: typography.fontSize.md, fontWeight: '500' },
  strikethrough: { textDecorationLine: 'line-through' },
  checkIcon: { marginLeft: spacing.xs },
  notes: { fontSize: typography.fontSize.xs },
  priority: { fontSize: typography.fontSize.xs, fontWeight: '600' },
  addBtn: { paddingHorizontal: spacing.xs },
  pressed: { opacity: 0.5 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md },
  emptyText: { fontSize: typography.fontSize.md },
});
