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
import { todoApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Card } from '../../../components/Card';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { TodoResponse, ImportanceLevel } from '../../../types/api';

const IMPORTANCE_COLOR: Record<ImportanceLevel, string> = {
  LOW: '#9CA3AF',
  MEDIUM: '#F59E0B',
  HIGH: '#F97316',
  CRITICAL: '#EF4444',
};

export default function TodoListScreen() {
  const colors = useThemeColors();
  const [todos, setTodos] = useState<TodoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [sortBy, setSortBy] = useState<'createdAt' | 'importance' | 'deadline'>('createdAt');
  const [showDone, setShowDone] = useState<boolean | undefined>(undefined);

  const load = useCallback(() => {
    setLoading(true);
    todoApi.list({ sortBy, showDone }).then((r) => {
      if (r.ok) setTodos(r.data);
      else setError('Could not load todos.');
      setLoading(false);
    });
  }, [sortBy, showDone]);

  useFocusEffect(load);

  return (
    <Screen scroll={false} padded={false}>
      <Stack.Screen
        options={{
          title: 'To-Do',
          headerRight: () => (
            <Pressable
              onPress={() => router.push('/(app)/todos/new')}
              style={({ pressed }) => [styles.addBtn, pressed && styles.pressed]}
              accessibilityLabel="New task"
            >
              <Ionicons name="add" size={28} color={colors.primary} />
            </Pressable>
          ),
        }}
      />

      <View style={[styles.filters, { borderBottomColor: colors.border }]}>
        {(['createdAt', 'importance', 'deadline'] as const).map((s) => (
          <Pressable
            key={s}
            onPress={() => setSortBy(s)}
            style={[styles.chip, sortBy === s && { backgroundColor: colors.primary }]}
          >
            <Text style={[styles.chipText, { color: sortBy === s ? '#fff' : colors.textSecondary }]}>
              {s === 'createdAt' ? 'Recent' : s.charAt(0).toUpperCase() + s.slice(1)}
            </Text>
          </Pressable>
        ))}
        <Pressable
          onPress={() => setShowDone(showDone == null ? false : showDone === false ? true : undefined)}
          style={[styles.chip, showDone != null && { backgroundColor: colors.primary }]}
        >
          <Text style={[styles.chipText, { color: showDone != null ? '#fff' : colors.textSecondary }]}>
            {showDone == null ? 'All' : showDone ? 'Done' : 'Active'}
          </Text>
        </Pressable>
      </View>

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : error ? (
        <View style={styles.center}>
          <Text style={{ color: colors.danger }}>{error}</Text>
        </View>
      ) : todos.length === 0 ? (
        <View style={styles.center}>
          <Ionicons name="checkmark-circle-outline" size={48} color={colors.textSecondary} />
          <Text style={[styles.emptyText, { color: colors.textSecondary }]}>No tasks yet</Text>
        </View>
      ) : (
        <FlatList
          data={todos}
          keyExtractor={(t) => t.id}
          contentContainerStyle={styles.list}
          renderItem={({ item }) => (
            <Pressable onPress={() => router.push(`/(app)/todos/${item.id}`)}>
              <Card style={[styles.card, item.isDone && styles.doneCard]}>
                <View style={styles.cardRow}>
                  <View
                    style={[styles.dot, { backgroundColor: IMPORTANCE_COLOR[item.importance] }]}
                  />
                  <Text
                    style={[
                      styles.cardTitle,
                      { color: colors.text },
                      item.isDone && styles.strikethrough,
                    ]}
                    numberOfLines={2}
                  >
                    {item.title}
                  </Text>
                  {item.isDone && (
                    <Ionicons name="checkmark-circle" size={18} color={colors.success} style={styles.checkIcon} />
                  )}
                </View>
                {item.deadline && (
                  <Text style={[styles.deadline, { color: colors.textSecondary }]}>
                    Due {new Date(item.deadline).toLocaleDateString()}
                  </Text>
                )}
              </Card>
            </Pressable>
          )}
        />
      )}
    </Screen>
  );
}

const styles = StyleSheet.create({
  filters: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.xs,
    padding: spacing.md,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  chip: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: 20,
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: '#D1D5DB',
  },
  chipText: { fontSize: typography.fontSize.xs },
  list: { padding: spacing.md, gap: spacing.sm },
  card: { padding: spacing.md },
  doneCard: { opacity: 0.6 },
  cardRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  dot: { width: 10, height: 10, borderRadius: 5 },
  cardTitle: { flex: 1, fontSize: typography.fontSize.md },
  strikethrough: { textDecorationLine: 'line-through' },
  checkIcon: { marginLeft: spacing.xs },
  deadline: { fontSize: typography.fontSize.xs, marginTop: spacing.xs, marginLeft: 18 },
  addBtn: { paddingHorizontal: spacing.xs },
  pressed: { opacity: 0.5 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md },
  emptyText: { fontSize: typography.fontSize.md },
});
