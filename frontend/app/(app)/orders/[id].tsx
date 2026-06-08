import React, { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { router, Stack, useLocalSearchParams } from 'expo-router';
import { orderApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Button } from '../../../components/Button';
import { Card } from '../../../components/Card';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { OrderResponse, OrderStatus } from '../../../types/api';

const ALL_STATUSES: OrderStatus[] = [
  'PENDING', 'CONFIRMED', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED', 'RETURNED',
];

export default function OrderDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const colors = useThemeColors();
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [changing, setChanging] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    orderApi.get(id).then((r) => {
      if (r.ok) setOrder(r.data);
      else setError('Could not load order.');
      setLoading(false);
    });
  }, [id]);

  async function changeStatus(newStatus: OrderStatus) {
    setChanging(true);
    const result = await orderApi.changeStatus(id, { status: newStatus });
    setChanging(false);
    if (result.ok) setOrder(result.data);
    else setError(result.error.message ?? 'Failed to update status.');
  }

  function confirmDelete() {
    Alert.alert('Delete Order', 'This cannot be undone.', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Delete', style: 'destructive', onPress: async () => {
          await orderApi.delete(id);
          router.back();
        },
      },
    ]);
  }

  if (loading) {
    return (
      <Screen scroll={false} padded>
        <Stack.Screen options={{ title: 'Order' }} />
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      </Screen>
    );
  }

  if (!order) {
    return (
      <Screen scroll={false} padded>
        <Stack.Screen options={{ title: 'Order' }} />
        <View style={styles.center}>
          <Text style={{ color: colors.danger }}>{error || 'Order not found.'}</Text>
        </View>
      </Screen>
    );
  }

  return (
    <Screen scroll padded>
      <Stack.Screen options={{ title: order.title }} />
      <ScrollView contentContainerStyle={styles.container}>

        <Card>
          <InfoRow label="Status" value={order.status.replace(/_/g, ' ')} colors={colors} />
          <InfoRow label="Source" value={order.source} colors={colors} />
          {order.merchant && <InfoRow label="Merchant" value={order.merchant} colors={colors} />}
          {order.amount != null && (
            <InfoRow label="Amount" value={`${order.currency ?? ''} ${order.amount.toFixed(2)}`} colors={colors} />
          )}
          {order.externalRef && <InfoRow label="Ref #" value={order.externalRef} colors={colors} />}
          <InfoRow label="Created" value={new Date(order.createdAt).toLocaleString()} colors={colors} />
        </Card>

        {order.description && (
          <Card>
            <Text style={[styles.noteText, { color: colors.text }]}>{order.description}</Text>
          </Card>
        )}

        <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>Change Status</Text>
        <View style={styles.statusGrid}>
          {ALL_STATUSES.map((s) => (
            <Button
              key={s}
              label={s.replace(/_/g, ' ')}
              onPress={() => changeStatus(s)}
              variant={order.status === s ? 'primary' : 'ghost'}
              loading={changing && order.status !== s}
              style={styles.statusBtn}
            />
          ))}
        </View>

        {order.statusHistory.length > 0 && (
          <>
            <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>History</Text>
            <Card>
              {order.statusHistory.map((h, i) => (
                <View
                  key={h.id}
                  style={[styles.historyRow, i > 0 && { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: colors.border }]}
                >
                  <Text style={[styles.historyStatus, { color: colors.text }]}>
                    {h.oldStatus ? `${h.oldStatus.replace(/_/g, ' ')} → ` : ''}
                    {h.newStatus.replace(/_/g, ' ')}
                  </Text>
                  <Text style={[styles.historyDate, { color: colors.textSecondary }]}>
                    {new Date(h.changedAt).toLocaleString()}
                  </Text>
                  {h.note && (
                    <Text style={[styles.historyNote, { color: colors.textSecondary }]}>{h.note}</Text>
                  )}
                </View>
              ))}
            </Card>
          </>
        )}

        {error ? <Text style={{ color: colors.danger }}>{error}</Text> : null}
        <Button label="Delete Order" onPress={confirmDelete} variant="ghost" style={styles.deleteBtn} />
      </ScrollView>
    </Screen>
  );
}

function InfoRow({ label, value, colors }: { label: string; value: string; colors: ReturnType<typeof useThemeColors> }) {
  return (
    <View style={[styles.infoRow, { borderTopColor: colors.border }]}>
      <Text style={[styles.infoLabel, { color: colors.textSecondary }]}>{label}</Text>
      <Text style={[styles.infoValue, { color: colors.text }]}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { gap: spacing.md },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: spacing.sm,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  infoLabel: { fontSize: typography.fontSize.sm },
  infoValue: { fontSize: typography.fontSize.sm, fontWeight: '500' },
  noteText: { fontSize: typography.fontSize.sm, lineHeight: 22 },
  sectionTitle: { fontSize: typography.fontSize.sm, fontWeight: '600', textTransform: 'uppercase', letterSpacing: 0.5 },
  statusGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.xs },
  statusBtn: { flex: 0 },
  historyRow: { paddingVertical: spacing.sm },
  historyStatus: { fontSize: typography.fontSize.sm, fontWeight: '500' },
  historyDate: { fontSize: typography.fontSize.xs, marginTop: 2 },
  historyNote: { fontSize: typography.fontSize.xs, marginTop: 2, fontStyle: 'italic' },
  deleteBtn: { marginTop: spacing.sm },
});
