import React, { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Image,
  Pressable,
  SectionList,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { router, Stack, useFocusEffect } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { bookApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Card } from '../../../components/Card';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { BookResponse, BookStatus } from '../../../types/api';

const SECTION_ORDER: { status: BookStatus; title: string }[] = [
  { status: 'IN_PROGRESS', title: 'In Progress' },
  { status: 'TO_READ', title: 'To Read' },
  { status: 'READ', title: 'Read' },
];

const STATUS_COLOR: Record<BookStatus, string> = {
  IN_PROGRESS: '#F59E0B',
  TO_READ: '#9CA3AF',
  READ: '#38A169',
};

export default function BooksScreen() {
  const colors = useThemeColors();
  const [books, setBooks] = useState<BookResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    bookApi.list().then((r) => {
      if (r.ok) setBooks(r.data);
      else setError('Could not load books.');
      setLoading(false);
    });
  }, []);

  useFocusEffect(load);

  const sections = useMemo(
    () =>
      SECTION_ORDER.map(({ status, title }) => ({
        title,
        status,
        data: books.filter((b) => b.status === status),
      })).filter((s) => s.data.length > 0),
    [books],
  );

  return (
    <Screen scroll={false} padded={false}>
      <Stack.Screen
        options={{
          title: 'Books to Read',
          headerRight: () => (
            <Pressable
              onPress={() => router.push('/(app)/books/new')}
              style={({ pressed }) => [styles.addBtn, pressed && styles.pressed]}
              accessibilityLabel="Add book"
            >
              <Ionicons name="add" size={28} color={colors.primary} />
            </Pressable>
          ),
        }}
      />

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : error ? (
        <View style={styles.center}>
          <Text style={{ color: colors.danger }}>{error}</Text>
        </View>
      ) : books.length === 0 ? (
        <View style={styles.center}>
          <Ionicons name="book-outline" size={48} color={colors.textSecondary} />
          <Text style={[styles.emptyText, { color: colors.textSecondary }]}>
            No books yet
          </Text>
        </View>
      ) : (
        <SectionList
          sections={sections}
          keyExtractor={(b) => b.id}
          contentContainerStyle={styles.list}
          stickySectionHeadersEnabled={false}
          renderSectionHeader={({ section }) => (
            <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>
              {section.title}
            </Text>
          )}
          renderItem={({ item }) => (
            <Pressable onPress={() => router.push(`/(app)/books/${item.id}`)}>
              <Card style={styles.card}>
                <CoverThumbnail uri={item.coverImageUrl} />
                <View style={styles.cardContent}>
                  <View style={styles.cardRow}>
                    <View style={[styles.dot, { backgroundColor: STATUS_COLOR[item.status] }]} />
                    <Text style={[styles.cardTitle, { color: colors.text }]} numberOfLines={2}>
                      {item.title}
                    </Text>
                  </View>
                  {item.startDate && (
                    <Text style={[styles.dates, { color: colors.textSecondary }]}>
                      Started {new Date(item.startDate).toLocaleDateString()}
                    </Text>
                  )}
                  {item.endDate && (
                    <Text style={[styles.dates, { color: colors.textSecondary }]}>
                      Finished {new Date(item.endDate).toLocaleDateString()}
                    </Text>
                  )}
                </View>
              </Card>
            </Pressable>
          )}
        />
      )}
    </Screen>
  );
}

function CoverThumbnail({ uri }: { uri: string | null }) {
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
      <Ionicons name="book-outline" size={28} color={colors.textSecondary} />
    </View>
  );
}

const styles = StyleSheet.create({
  list: { padding: spacing.md, gap: spacing.sm },
  sectionTitle: {
    fontSize: typography.fontSize.sm,
    fontWeight: typography.fontWeight.semibold,
    textTransform: 'uppercase',
    marginTop: spacing.md,
    marginBottom: spacing.sm,
  },
  card: { padding: spacing.sm, flexDirection: 'row', alignItems: 'center', gap: spacing.md, marginBottom: spacing.sm },
  thumbnail: { width: 56, height: 80, borderRadius: 6 },
  thumbnailPlaceholder: { alignItems: 'center', justifyContent: 'center' },
  cardContent: { flex: 1, gap: spacing.xs },
  cardRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  dot: { width: 8, height: 8, borderRadius: 4, flexShrink: 0 },
  cardTitle: { flex: 1, fontSize: typography.fontSize.md, fontWeight: '500' },
  dates: { fontSize: typography.fontSize.xs },
  addBtn: { paddingHorizontal: spacing.xs },
  pressed: { opacity: 0.5 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md },
  emptyText: { fontSize: typography.fontSize.md },
});
