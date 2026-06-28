import React, { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Image,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { router, Stack, useLocalSearchParams } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { articleApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Button } from '../../../components/Button';
import { FormField } from '../../../components/FormField';
import { Card } from '../../../components/Card';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { ArticleResponse, ArticleStatus } from '../../../types/api';

const STATUS_OPTIONS: { value: ArticleStatus; label: string }[] = [
  { value: 'TO_READ', label: 'Waiting' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'READ', label: 'Read' },
];

export default function ArticleDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const colors = useThemeColors();
  const [article, setArticle] = useState<ArticleResponse | null>(null);
  const [title, setTitle] = useState('');
  const [coverImageUrl, setCoverImageUrl] = useState('');
  const [sourceUrl, setSourceUrl] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [imageErrored, setImageErrored] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [changingStatus, setChangingStatus] = useState<ArticleStatus | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    articleApi.get(id).then((r) => {
      if (r.ok) {
        populate(r.data);
      } else {
        setError('Could not load article.');
      }
      setLoading(false);
    });
  }, [id]);

  function populate(data: ArticleResponse) {
    setArticle(data);
    setTitle(data.title);
    setCoverImageUrl(data.coverImageUrl ?? '');
    setSourceUrl(data.sourceUrl ?? '');
    setStartDate(data.startDate ?? '');
    setEndDate(data.endDate ?? '');
    setImageErrored(false);
  }

  async function handleSave() {
    if (!title.trim()) { setError('Title is required.'); return; }
    if (!article) return;
    setSaving(true);
    setError('');
    const result = await articleApi.update(id, {
      title: title.trim(),
      coverImageUrl: coverImageUrl.trim() || undefined,
      sourceUrl: sourceUrl.trim() || undefined,
      status: article.status,
      startDate: startDate.trim() || null,
      endDate: endDate.trim() || null,
    });
    setSaving(false);
    if (result.ok) {
      router.back();
    } else {
      setError(result.error.message ?? 'Failed to save.');
    }
  }

  async function handleStatusChange(status: ArticleStatus) {
    if (!article || status === article.status) return;
    setChangingStatus(status);
    const result = await articleApi.updateStatus(id, { status });
    setChangingStatus(null);
    if (result.ok) {
      populate(result.data);
    } else {
      setError(result.error.message ?? 'Failed to update status.');
    }
  }

  async function handleRefreshMetadata() {
    setRefreshing(true);
    const result = await articleApi.refreshMetadata(id);
    setRefreshing(false);
    if (result.ok) {
      populate(result.data);
    } else {
      setError(result.error.message ?? 'Failed to refresh from link.');
    }
  }

  function executeDelete() {
    setDeleting(true);
    articleApi
      .delete(id)
      .then((result) => {
        if (result.ok) {
          router.back();
        } else {
          setDeleting(false);
          setError(result.error?.message ?? 'Failed to delete article.');
        }
      })
      .catch((e: unknown) => {
        setDeleting(false);
        setError((e as Error)?.message ?? 'Unexpected error.');
      });
  }

  function confirmDelete() {
    if (Platform.OS === 'web') {
      if (window.confirm('Remove this article from your list?')) {
        executeDelete();
      }
      return;
    }
    Alert.alert('Remove Article', 'Remove this article from your list?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Remove', style: 'destructive', onPress: executeDelete },
    ]);
  }

  if (loading) {
    return (
      <Screen scroll={false} padded>
        <Stack.Screen options={{ title: 'Article' }} />
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      </Screen>
    );
  }

  const imageUri = coverImageUrl && !imageErrored ? coverImageUrl : null;

  return (
    <Screen scroll padded>
      <Stack.Screen options={{ title: article?.title ?? 'Article' }} />
      <ScrollView contentContainerStyle={styles.container}>

        <Card style={styles.imageCard}>
          {imageUri ? (
            <Image
              source={{ uri: imageUri }}
              style={styles.coverImage}
              resizeMode="contain"
              onError={() => setImageErrored(true)}
            />
          ) : (
            <View style={[styles.coverImage, styles.imagePlaceholder, { backgroundColor: colors.border }]}>
              <Ionicons name="newspaper-outline" size={52} color={colors.textSecondary} />
              <Text style={[styles.placeholderText, { color: colors.textSecondary }]}>No cover</Text>
            </View>
          )}
          {sourceUrl ? (
            <Button
              label={refreshing ? 'Fetching…' : 'Refresh from Link'}
              onPress={handleRefreshMetadata}
              loading={refreshing}
              variant="ghost"
              style={styles.refreshBtn}
            />
          ) : null}
        </Card>

        <Text style={[styles.label, { color: colors.textSecondary }]}>Status</Text>
        <View style={styles.chips}>
          {STATUS_OPTIONS.map((opt) => (
            <Button
              key={opt.value}
              label={changingStatus === opt.value ? 'Updating…' : opt.label}
              onPress={() => handleStatusChange(opt.value)}
              loading={changingStatus === opt.value}
              variant={article?.status === opt.value ? 'primary' : 'ghost'}
              style={styles.chip}
            />
          ))}
        </View>

        <FormField label="Title" value={title} onChangeText={setTitle} placeholder="Article title" />
        <FormField
          label="Article link (optional)"
          value={sourceUrl}
          onChangeText={setSourceUrl}
          placeholder="https://…"
          autoCapitalize="none"
          keyboardType="url"
        />
        <FormField
          label="Cover photo URL (optional)"
          value={coverImageUrl}
          onChangeText={setCoverImageUrl}
          placeholder="https://…"
          autoCapitalize="none"
          keyboardType="url"
        />
        <FormField
          label="Start date"
          value={startDate}
          onChangeText={setStartDate}
          placeholder="YYYY-MM-DD"
          autoCapitalize="none"
        />
        <FormField
          label="End date"
          value={endDate}
          onChangeText={setEndDate}
          placeholder="YYYY-MM-DD"
          autoCapitalize="none"
        />

        {error ? <Text style={[styles.error, { color: colors.danger }]}>{error}</Text> : null}

        <Button label="Save Changes" onPress={handleSave} loading={saving} style={styles.submit} />
        <Button label="Remove Article" onPress={confirmDelete} loading={deleting} variant="ghost" style={styles.delete} />
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: { gap: spacing.md },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  imageCard: { alignItems: 'center', padding: spacing.md, gap: spacing.sm },
  coverImage: { width: '100%', height: 220, borderRadius: 10 },
  imagePlaceholder: { alignItems: 'center', justifyContent: 'center', gap: spacing.sm },
  placeholderText: { fontSize: typography.fontSize.sm },
  refreshBtn: { alignSelf: 'center' },
  label: { fontSize: typography.fontSize.sm, marginBottom: spacing.xs },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.xs },
  chip: { flex: 0 },
  error: { fontSize: typography.fontSize.sm },
  submit: { marginTop: spacing.sm },
  delete: { marginTop: spacing.xs },
});
