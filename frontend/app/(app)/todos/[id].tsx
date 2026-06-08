import React, { useEffect, useState } from 'react';
import { ActivityIndicator, Alert, ScrollView, StyleSheet, Text, View } from 'react-native';
import { router, Stack, useLocalSearchParams } from 'expo-router';
import { todoApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Button } from '../../../components/Button';
import { FormField } from '../../../components/FormField';
import { Card } from '../../../components/Card';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { ImportanceLevel, TodoResponse } from '../../../types/api';

const IMPORTANCE_OPTIONS: ImportanceLevel[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function TodoDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const colors = useThemeColors();
  const [todo, setTodo] = useState<TodoResponse | null>(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [importance, setImportance] = useState<ImportanceLevel>('MEDIUM');
  const [deadline, setDeadline] = useState('');
  const [isDone, setIsDone] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    todoApi.get(id).then((r) => {
      if (r.ok) {
        const t = r.data;
        setTodo(t);
        setTitle(t.title);
        setDescription(t.description ?? '');
        setImportance(t.importance);
        setDeadline(t.deadline ?? '');
        setIsDone(t.isDone);
      } else {
        setError('Could not load task.');
      }
      setLoading(false);
    });
  }, [id]);

  async function handleSave() {
    if (!title.trim()) { setError('Title is required.'); return; }
    setSaving(true);
    setError('');
    const result = await todoApi.update(id, {
      title: title.trim(),
      description: description.trim() || undefined,
      importance,
      deadline: deadline.trim() || null,
      isDone,
    });
    setSaving(false);
    if (result.ok) {
      router.back();
    } else {
      setError(result.error.message ?? 'Failed to save.');
    }
  }

  function confirmDelete() {
    Alert.alert('Delete Task', 'Are you sure?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Delete', style: 'destructive', onPress: async () => {
          await todoApi.delete(id);
          router.back();
        },
      },
    ]);
  }

  if (loading) {
    return (
      <Screen scroll={false} padded>
        <Stack.Screen options={{ title: 'Task' }} />
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      </Screen>
    );
  }

  return (
    <Screen scroll padded>
      <Stack.Screen options={{ title: todo?.title ?? 'Task' }} />
      <ScrollView contentContainerStyle={styles.container}>
        <Card style={styles.meta}>
          <Text style={[styles.metaText, { color: colors.textSecondary }]}>
            Created {todo ? new Date(todo.createdAt).toLocaleString() : ''}
          </Text>
        </Card>

        <FormField label="Title" value={title} onChangeText={setTitle} placeholder="Task title" />
        <FormField
          label="Description (optional)"
          value={description}
          onChangeText={setDescription}
          placeholder="Details…"
          multiline
        />

        <Text style={[styles.label, { color: colors.textSecondary }]}>Importance</Text>
        <View style={styles.chips}>
          {IMPORTANCE_OPTIONS.map((lvl) => (
            <Button
              key={lvl}
              label={lvl}
              onPress={() => setImportance(lvl)}
              variant={importance === lvl ? 'primary' : 'ghost'}
              style={styles.chip}
            />
          ))}
        </View>

        <FormField
          label="Deadline (optional)"
          value={deadline}
          onChangeText={setDeadline}
          placeholder="2025-12-31T00:00:00Z"
          autoCapitalize="none"
        />

        <Button
          label={isDone ? 'Mark as Active' : 'Mark as Done'}
          onPress={() => setIsDone(!isDone)}
          variant="ghost"
        />

        {error ? <Text style={[styles.error, { color: colors.danger }]}>{error}</Text> : null}

        <Button label="Save Changes" onPress={handleSave} loading={saving} style={styles.submit} />
        <Button label="Delete Task" onPress={confirmDelete} variant="ghost" style={styles.delete} />
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: { gap: spacing.md },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  meta: { padding: spacing.sm },
  metaText: { fontSize: typography.fontSize.xs },
  label: { fontSize: typography.fontSize.sm },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.xs },
  chip: { flex: 0 },
  error: { fontSize: typography.fontSize.sm },
  submit: { marginTop: spacing.sm },
  delete: { marginTop: spacing.xs },
});
