import React, { useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { router, Stack } from 'expo-router';
import { wishlistApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Button } from '../../../components/Button';
import { FormField } from '../../../components/FormField';
import { spacing, typography, useThemeColors } from '../../../theme';
import type { WishlistPriority } from '../../../types/api';

const PRIORITY_OPTIONS: WishlistPriority[] = ['LOW', 'MEDIUM', 'HIGH'];

export default function NewWishlistItemScreen() {
  const colors = useThemeColors();
  const [name, setName] = useState('');
  const [productUrl, setProductUrl] = useState('');
  const [notes, setNotes] = useState('');
  const [priority, setPriority] = useState<WishlistPriority>('MEDIUM');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleCreate() {
    if (!name.trim()) { setError('Name is required.'); return; }
    if (!productUrl.trim()) { setError('Product URL is required.'); return; }
    setLoading(true);
    setError('');
    const result = await wishlistApi.create({
      name: name.trim(),
      productUrl: productUrl.trim(),
      notes: notes.trim() || undefined,
      priority,
    });
    setLoading(false);
    if (result.ok) {
      router.back();
    } else {
      setError(result.error.message ?? 'Failed to add item.');
    }
  }

  return (
    <Screen scroll padded>
      <Stack.Screen options={{ title: 'Add to Wishlist' }} />
      <ScrollView contentContainerStyle={styles.container}>
        <FormField
          label="Product name"
          value={name}
          onChangeText={setName}
          placeholder="AirPods Pro"
          autoFocus
        />
        <FormField
          label="Product URL"
          value={productUrl}
          onChangeText={setProductUrl}
          placeholder="https://www.amazon.com/…"
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
          placeholder="Birthday gift idea…"
          multiline
        />

        <Text style={[styles.hint, { color: colors.textSecondary }]}>
          The product image will be fetched automatically from the URL.
        </Text>

        {error ? <Text style={[styles.error, { color: colors.danger }]}>{error}</Text> : null}

        <Button
          label={loading ? 'Fetching image…' : 'Add to Wishlist'}
          onPress={handleCreate}
          loading={loading}
          style={styles.submit}
        />
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: { gap: spacing.md },
  label: { fontSize: typography.fontSize.sm, marginBottom: spacing.xs },
  chips: { flexDirection: 'row', gap: spacing.xs },
  chip: { flex: 0 },
  hint: { fontSize: typography.fontSize.xs, fontStyle: 'italic' },
  error: { fontSize: typography.fontSize.sm },
  submit: { marginTop: spacing.sm },
});
