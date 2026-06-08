import React, { useState } from 'react';
import { ScrollView, StyleSheet, Text } from 'react-native';
import { router, Stack } from 'expo-router';
import { orderApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Button } from '../../../components/Button';
import { FormField } from '../../../components/FormField';
import { spacing, useThemeColors } from '../../../theme';

export default function NewOrderScreen() {
  const colors = useThemeColors();
  const [title, setTitle] = useState('');
  const [merchant, setMerchant] = useState('');
  const [amount, setAmount] = useState('');
  const [currency, setCurrency] = useState('');
  const [externalRef, setExternalRef] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleCreate() {
    if (!title.trim()) { setError('Title is required.'); return; }
    const parsedAmount = amount.trim() ? parseFloat(amount.trim()) : undefined;
    if (amount.trim() && isNaN(parsedAmount!)) { setError('Amount must be a number.'); return; }
    setLoading(true);
    setError('');
    const result = await orderApi.create({
      title: title.trim(),
      description: description.trim() || undefined,
      merchant: merchant.trim() || undefined,
      amount: parsedAmount,
      currency: currency.trim() || undefined,
      externalRef: externalRef.trim() || undefined,
    });
    setLoading(false);
    if (result.ok) {
      router.back();
    } else {
      setError(result.error.message ?? 'Failed to create order.');
    }
  }

  return (
    <Screen scroll padded>
      <Stack.Screen options={{ title: 'New Order' }} />
      <ScrollView contentContainerStyle={styles.container}>
        <FormField label="Title" value={title} onChangeText={setTitle} placeholder="AirPods Pro" autoFocus />
        <FormField label="Merchant (optional)" value={merchant} onChangeText={setMerchant} placeholder="Amazon" />
        <FormField label="Amount (optional)" value={amount} onChangeText={setAmount} placeholder="49.99" keyboardType="decimal-pad" />
        <FormField label="Currency (optional)" value={currency} onChangeText={setCurrency} placeholder="USD" autoCapitalize="characters" />
        <FormField label="Order / Ref # (optional)" value={externalRef} onChangeText={setExternalRef} placeholder="ORD-12345" autoCapitalize="none" />
        <FormField label="Notes (optional)" value={description} onChangeText={setDescription} placeholder="…" multiline />

        {error ? <Text style={[styles.error, { color: colors.danger }]}>{error}</Text> : null}

        <Button label="Create Order" onPress={handleCreate} loading={loading} style={styles.submit} />
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: { gap: spacing.md },
  error: { fontSize: 14 },
  submit: { marginTop: spacing.sm },
});
