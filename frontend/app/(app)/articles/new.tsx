import React, { useState } from 'react';
import { Image, ScrollView, StyleSheet, Text, View } from 'react-native';
import { router, Stack } from 'expo-router';
import { articleApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Button } from '../../../components/Button';
import { FormField } from '../../../components/FormField';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { ArticleStatus } from '../../../types/api';

const STATUS_OPTIONS: { value: ArticleStatus; label: string }[] = [
  { value: 'TO_READ', label: 'To Read' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'READ', label: 'Read' },
];

export default function NewArticleScreen() {
  const colors = useThemeColors();
  const [sourceUrl, setSourceUrl] = useState('');
  const [title, setTitle] = useState('');
  const [coverImageUrl, setCoverImageUrl] = useState('');
  const [status, setStatus] = useState<ArticleStatus>('TO_READ');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [fetching, setFetching] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function handleFetchFromLink() {
    if (!sourceUrl.trim()) { setError('Enter a link first.'); return; }
    setFetching(true);
    setError('');
    const result = await articleApi.fetchMetadataPreview(sourceUrl.trim());
    setFetching(false);
    if (result.ok) {
      if (result.data.title) setTitle(result.data.title);
      if (result.data.coverImageUrl) setCoverImageUrl(result.data.coverImageUrl);
      if (!result.data.title && !result.data.coverImageUrl) {
        setError('Could not find a title or photo at that link — enter them manually.');
      }
    } else {
      setError(result.error.message ?? 'Failed to fetch from link.');
    }
  }

  async function handleCreate() {
    if (!title.trim() && !sourceUrl.trim()) {
      setError('Enter a title, or a link to fetch one from.');
      return;
    }
    setSaving(true);
    setError('');
    const result = await articleApi.create({
      title: title.trim() || undefined,
      coverImageUrl: coverImageUrl.trim() || undefined,
      sourceUrl: sourceUrl.trim() || undefined,
      status,
      startDate: startDate.trim() || undefined,
      endDate: endDate.trim() || undefined,
    });
    setSaving(false);
    if (result.ok) {
      router.back();
    } else {
      setError(result.error.message ?? 'Failed to add article.');
    }
  }

  return (
    <Screen scroll padded>
      <Stack.Screen options={{ title: 'Add Article' }} />
      <ScrollView contentContainerStyle={styles.container}>
        <FormField
          label="Article link (optional)"
          value={sourceUrl}
          onChangeText={setSourceUrl}
          placeholder="https://www.example.com/article…"
          autoCapitalize="none"
          keyboardType="url"
        />
        <Button
          label={fetching ? 'Fetching…' : 'Fetch Title & Photo from Link'}
          onPress={handleFetchFromLink}
          loading={fetching}
          variant="ghost"
          style={styles.fetchBtn}
        />

        {coverImageUrl ? (
          <Image source={{ uri: coverImageUrl }} style={styles.preview} resizeMode="contain" />
        ) : null}

        <FormField
          label="Title"
          value={title}
          onChangeText={setTitle}
          placeholder="Article title"
          autoFocus
        />
        <FormField
          label="Cover photo URL (optional)"
          value={coverImageUrl}
          onChangeText={setCoverImageUrl}
          placeholder="https://…"
          autoCapitalize="none"
          keyboardType="url"
        />

        <Text style={[styles.label, { color: colors.textSecondary }]}>Status</Text>
        <View style={styles.chips}>
          {STATUS_OPTIONS.map((opt) => (
            <Button
              key={opt.value}
              label={opt.label}
              onPress={() => setStatus(opt.value)}
              variant={status === opt.value ? 'primary' : 'ghost'}
              style={styles.chip}
            />
          ))}
        </View>

        {status !== 'TO_READ' && (
          <FormField
            label="Start date (optional, defaults to today)"
            value={startDate}
            onChangeText={setStartDate}
            placeholder="YYYY-MM-DD"
            autoCapitalize="none"
          />
        )}
        {status === 'READ' && (
          <FormField
            label="End date (optional, defaults to today)"
            value={endDate}
            onChangeText={setEndDate}
            placeholder="YYYY-MM-DD"
            autoCapitalize="none"
          />
        )}

        {error ? <Text style={[styles.error, { color: colors.danger }]}>{error}</Text> : null}

        <Button
          label={saving ? 'Saving…' : 'Add Article'}
          onPress={handleCreate}
          loading={saving}
          style={styles.submit}
        />
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: { gap: spacing.md },
  fetchBtn: { marginTop: -spacing.sm },
  preview: { width: '100%', height: 180, borderRadius: 10 },
  label: { fontSize: typography.fontSize.sm, marginBottom: spacing.xs },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.xs },
  chip: { flex: 0 },
  error: { fontSize: typography.fontSize.sm },
  submit: { marginTop: spacing.sm },
});
