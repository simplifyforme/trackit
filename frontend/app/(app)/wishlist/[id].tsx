import React, { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Image,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { router, Stack, useLocalSearchParams } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { wishlistApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Button } from '../../../components/Button';
import { FormField } from '../../../components/FormField';
import { Card } from '../../../components/Card';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { WishlistItemResponse, WishlistPriority } from '../../../types/api';

const PRIORITY_OPTIONS: WishlistPriority[] = ['LOW', 'MEDIUM', 'HIGH'];

export default function WishlistItemDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const colors = useThemeColors();
  const [item, setItem] = useState<WishlistItemResponse | null>(null);
  const [name, setName] = useState('');
  const [productUrl, setProductUrl] = useState('');
  const [notes, setNotes] = useState('');
  const [priority, setPriority] = useState<WishlistPriority>('MEDIUM');
  const [isPurchased, setIsPurchased] = useState(false);
  const [imageErrored, setImageErrored] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [refreshingImage, setRefreshingImage] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    wishlistApi.get(id).then((r) => {
      if (r.ok) {
        populate(r.data);
      } else {
        setError('Could not load item.');
      }
      setLoading(false);
    });
  }, [id]);

  function populate(data: WishlistItemResponse) {
    setItem(data);
    setName(data.name);
    setProductUrl(data.productUrl);
    setNotes(data.notes ?? '');
    setPriority(data.priority);
    setIsPurchased(data.isPurchased);
    setImageErrored(false);
  }

  async function handleSave() {
    if (!name.trim()) { setError('Name is required.'); return; }
    if (!productUrl.trim()) { setError('Product URL is required.'); return; }
    setSaving(true);
    setError('');
    const result = await wishlistApi.update(id, {
      name: name.trim(),
      productUrl: productUrl.trim(),
      notes: notes.trim() || undefined,
      priority,
      isPurchased,
    });
    setSaving(false);
    if (result.ok) {
      router.back();
    } else {
      setError(result.error.message ?? 'Failed to save.');
    }
  }

  async function handleRefreshImage() {
    setRefreshingImage(true);
    const result = await wishlistApi.refreshImage(id);
    setRefreshingImage(false);
    if (result.ok) {
      populate(result.data);
    }
  }

  function confirmDelete() {
    Alert.alert('Remove Item', 'Remove this item from your wishlist?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Remove',
        style: 'destructive',
        onPress: async () => {
          await wishlistApi.delete(id);
          router.back();
        },
      },
    ]);
  }

  if (loading) {
    return (
      <Screen scroll={false} padded>
        <Stack.Screen options={{ title: 'Wishlist Item' }} />
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      </Screen>
    );
  }

  const imageUri = item?.imageUrl && !imageErrored ? item.imageUrl : null;

  return (
    <Screen scroll padded>
      <Stack.Screen options={{ title: item?.name ?? 'Wishlist Item' }} />
      <ScrollView contentContainerStyle={styles.container}>

        {/* Product image */}
        <Card style={styles.imageCard}>
          {imageUri ? (
            <Image
              source={{ uri: imageUri }}
              style={styles.productImage}
              resizeMode="contain"
              onError={() => setImageErrored(true)}
            />
          ) : (
            <View style={[styles.productImage, styles.imagePlaceholder, { backgroundColor: colors.border }]}>
              <Ionicons name="bag-outline" size={52} color={colors.textSecondary} />
              <Text style={[styles.placeholderText, { color: colors.textSecondary }]}>No image</Text>
            </View>
          )}
          <Button
            label={refreshingImage ? 'Fetching…' : 'Refresh Image'}
            onPress={handleRefreshImage}
            loading={refreshingImage}
            variant="ghost"
            style={styles.refreshBtn}
          />
        </Card>

        <Card style={styles.meta}>
          <Text style={[styles.metaText, { color: colors.textSecondary }]}>
            Added {item ? new Date(item.createdAt).toLocaleString() : ''}
          </Text>
        </Card>

        <FormField label="Product name" value={name} onChangeText={setName} placeholder="Product name" />
        <FormField
          label="Product URL"
          value={productUrl}
          onChangeText={setProductUrl}
          placeholder="https://…"
          autoCapitalize="none"
          keyboardType="url"
        />

        <Text style={[styles.label, { color: colors.textSecondary }]}>Priority</Text>
        <View style={styles.chips}>
          {PRIORITY_OPTIONS.map((p) => (
            <Button
              key={p}
              label={p}
              onPress={() => setPriority(p)}
              variant={priority === p ? 'primary' : 'ghost'}
              style={styles.chip}
            />
          ))}
        </View>

        <FormField
          label="Notes (optional)"
          value={notes}
          onChangeText={setNotes}
          placeholder="Details…"
          multiline
        />

        <Button
          label={isPurchased ? 'Mark as Not Purchased' : 'Mark as Purchased'}
          onPress={() => setIsPurchased(!isPurchased)}
          variant="ghost"
        />

        {error ? <Text style={[styles.error, { color: colors.danger }]}>{error}</Text> : null}

        <Button label="Save Changes" onPress={handleSave} loading={saving} style={styles.submit} />
        <Button label="Remove from Wishlist" onPress={confirmDelete} variant="ghost" style={styles.delete} />
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: { gap: spacing.md },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  imageCard: { alignItems: 'center', padding: spacing.md, gap: spacing.sm },
  productImage: { width: '100%', height: 200, borderRadius: 10 },
  imagePlaceholder: { alignItems: 'center', justifyContent: 'center', gap: spacing.sm },
  placeholderText: { fontSize: typography.fontSize.sm },
  refreshBtn: { alignSelf: 'center' },
  meta: { padding: spacing.sm },
  metaText: { fontSize: typography.fontSize.xs },
  label: { fontSize: typography.fontSize.sm, marginBottom: spacing.xs },
  chips: { flexDirection: 'row', gap: spacing.xs },
  chip: { flex: 0 },
  error: { fontSize: typography.fontSize.sm },
  submit: { marginTop: spacing.sm },
  delete: { marginTop: spacing.xs },
});
