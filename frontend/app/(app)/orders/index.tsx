import React, { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { router, Stack, useFocusEffect } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { orderApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Card } from '../../../components/Card';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { OrderResponse, OrderStatus } from '../../../types/api';

const STATUS_COLORS: Record<OrderStatus, string> = {
  PENDING:          '#9CA3AF',
  CONFIRMED:        '#3B82F6',
  SHIPPED:          '#8B5CF6',
  OUT_FOR_DELIVERY: '#F59E0B',
  DELIVERED:        '#10B981',
  CANCELLED:        '#EF4444',
  RETURNED:         '#F97316',
  NEEDS_REVIEW:     '#EC4899',
};

const STATUS_FILTERS: Array<OrderStatus | 'ALL'> = [
  'ALL', 'PENDING', 'CONFIRMED', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED',
];

export default function OrderListScreen() {
  const colors = useThemeColors();
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState<OrderStatus | 'ALL'>('ALL');

  const load = useCallback(() => {
    setLoading(true);
    orderApi.list({ status: statusFilter === 'ALL' ? undefined : statusFilter }).then((r) => {
      if (r.ok) setOrders(r.data);
      else setError('Could not load orders.');
      setLoading(false);
    });
  }, [statusFilter]);

  useFocusEffect(load);

  return (
    <Screen scroll={false} padded={false}>
      <Stack.Screen
        options={{
          title: 'Orders',
          headerRight: () => (
            <Pressable
              onPress={() => router.push('/(app)/orders/new')}
              style={({ pressed }) => [styles.addBtn, pressed && styles.pressed]}
              accessibilityLabel="New order"
            >
              <Ionicons name="add" size={28} color={colors.primary} />
            </Pressable>
          ),
        }}
      />

      <FlatList
        horizontal
        data={STATUS_FILTERS}
        keyExtractor={(s) => s}
        contentContainerStyle={styles.filterList}
        showsHorizontalScrollIndicator={false}
        renderItem={({ item }) => (
          <Pressable
            onPress={() => setStatusFilter(item)}
            style={[styles.chip, statusFilter === item && { backgroundColor: colors.primary }]}
          >
            <Text style={[styles.chipText, { color: statusFilter === item ? '#fff' : colors.textSecondary }]}>
              {item === 'ALL' ? 'All' : item.replace('_', ' ')}
            </Text>
          </Pressable>
        )}
      />

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : error ? (
        <View style={styles.center}>
          <Text style={{ color: colors.danger }}>{error}</Text>
        </View>
      ) : orders.length === 0 ? (
        <View style={styles.center}>
          <Ionicons name="bag-outline" size={48} color={colors.textSecondary} />
          <Text style={[styles.emptyText, { color: colors.textSecondary }]}>No orders yet</Text>
        </View>
      ) : (
        <FlatList
          data={orders}
          keyExtractor={(o) => o.id}
          contentContainerStyle={styles.list}
          renderItem={({ item }) => (
            <Pressable onPress={() => router.push(`/(app)/orders/${item.id}`)}>
              <Card style={styles.card}>
                <View style={styles.cardHeader}>
                  <Text style={[styles.cardTitle, { color: colors.text }]} numberOfLines={1}>
                    {item.title}
                  </Text>
                  <View style={[styles.badge, { backgroundColor: STATUS_COLORS[item.status] + '20' }]}>
                    <Text style={[styles.badgeText, { color: STATUS_COLORS[item.status] }]}>
                      {item.status.replace(/_/g, ' ')}
                    </Text>
                  </View>
                </View>
                {item.merchant && (
                  <Text style={[styles.sub, { color: colors.textSecondary }]}>{item.merchant}</Text>
                )}
                {item.amount != null && (
                  <Text style={[styles.amount, { color: colors.text }]}>
                    {item.currency ?? ''} {item.amount.toFixed(2)}
                  </Text>
                )}
                <Text style={[styles.date, { color: colors.textSecondary }]}>
                  {new Date(item.createdAt).toLocaleDateString()}
                  {item.source === 'EMAIL' ? '  · via email' : ''}
                </Text>
              </Card>
            </Pressable>
          )}
        />
      )}
    </Screen>
  );
}

const styles = StyleSheet.create({
  filterList: { padding: spacing.md, gap: spacing.xs },
  chip: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#D1D5DB',
    marginRight: spacing.xs,
  },
  chipText: { fontSize: typography.fontSize.xs },
  list: { padding: spacing.md, gap: spacing.sm },
  card: { padding: spacing.md },
  cardHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: spacing.sm },
  cardTitle: { flex: 1, fontSize: typography.fontSize.md, fontWeight: '500' },
  badge: { paddingHorizontal: spacing.sm, paddingVertical: 2, borderRadius: 12 },
  badgeText: { fontSize: typography.fontSize.xs, fontWeight: '600' },
  sub: { fontSize: typography.fontSize.sm, marginTop: spacing.xs },
  amount: { fontSize: typography.fontSize.sm, fontWeight: '500', marginTop: spacing.xs },
  date: { fontSize: typography.fontSize.xs, marginTop: spacing.xs },
  addBtn: { paddingHorizontal: spacing.xs },
  pressed: { opacity: 0.5 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md },
  emptyText: { fontSize: typography.fontSize.md },
});
