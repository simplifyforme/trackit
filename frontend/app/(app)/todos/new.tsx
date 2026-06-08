import React, { useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { router, Stack } from 'expo-router';
import { todoApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Button } from '../../../components/Button';
import { FormField } from '../../../components/FormField';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { ImportanceLevel } from '../../../types/api';

const IMPORTANCE_OPTIONS: ImportanceLevel[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function NewTodoScreen() {
  const colors = useThemeColors();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [importance, setImportance] = useState<ImportanceLevel>('MEDIUM');
  const [deadline, setDeadline] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleCreate() {
    if (!title.trim()) { setError('Title is required.'); return; }
    setLoading(true);
    setError('');
    const result = await todoApi.create({
      title: title.trim(),
      description: description.trim() || undefined,
      importance,
      deadline: deadline.trim() || undefined,
    });
    setLoading(false);
    if (result.ok) {
      router.back();
    } else {
      setError(result.error.message ?? 'Failed to create task.');
    }
  }

  return (
    <Screen scroll padded>
      <Stack.Screen options={{ title: 'New Task' }} />
      <ScrollView contentContainerStyle={styles.container}>
        <FormField
          label="Title"
          value={title}
          onChangeText={setTitle}
          placeholder="Buy groceries"
          autoFocus
        />
        <FormField
          label="Description (optional)"
          value={description}
          onChangeText={setDescription}
          placeholder="More details…"
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

        {error ? <Text style={[styles.error, { color: colors.danger }]}>{error}</Text> : null}

        <Button label="Create Task" onPress={handleCreate} loading={loading} style={styles.submit} />
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: { gap: spacing.md },
  label: { fontSize: typography.fontSize.sm, marginBottom: spacing.xs },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.xs },
  chip: { flex: 0 },
  error: { fontSize: typography.fontSize.sm },
  submit: { marginTop: spacing.sm },
});
