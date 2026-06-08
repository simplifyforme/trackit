import React, { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { authApi } from '../../lib/api/endpoints';
import { Screen } from '../../components/Screen';
import { Card } from '../../components/Card';
import { FormField } from '../../components/FormField';
import { Button } from '../../components/Button';
import { spacing, typography, useThemeColors } from '../../theme';

export default function ForgotPasswordScreen() {
  const colors = useThemeColors();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  async function handleSubmit() {
    if (!email.trim()) { setError('Email is required.'); return; }
    if (!/\S+@\S+\.\S+/.test(email)) { setError('Enter a valid email.'); return; }
    setError('');
    setLoading(true);
    // Backend always returns 200 regardless of whether the email exists
    // (enumeration prevention), so we don't branch on the result.
    await authApi.forgotPassword(email.trim().toLowerCase());
    setLoading(false);
    setSent(true);
  }

  if (sent) {
    return (
      <Screen scroll center>
        <View style={styles.container}>
          <Card style={styles.successCard}>
            <Text style={styles.icon}>✉️</Text>
            <Text style={[styles.title, { color: colors.text }]}>Check your email</Text>
            <Text style={[styles.body, { color: colors.textSecondary }]}>
              If that address is registered, you'll receive a reset link shortly.
            </Text>
            <Button
              label="Back to Sign In"
              onPress={() => router.replace('/(auth)/login')}
              style={styles.button}
            />
          </Card>
        </View>
      </Screen>
    );
  }

  return (
    <Screen scroll center>
      <View style={styles.container}>
        <Text style={[styles.title, { color: colors.text }]}>Reset password</Text>
        <Text style={[styles.body, { color: colors.textSecondary }]}>
          Enter your email and we'll send you a link to reset your password.
        </Text>

        <Card style={styles.card}>
          <FormField
            label="Email"
            value={email}
            onChangeText={setEmail}
            error={error}
            keyboardType="email-address"
            textContentType="emailAddress"
            returnKeyType="done"
            onSubmitEditing={handleSubmit}
            placeholder="you@example.com"
          />
          <Button
            label="Send reset link"
            onPress={handleSubmit}
            loading={loading}
            style={styles.button}
          />
        </Card>

        <Button label="Back to Sign In" onPress={() => router.back()} variant="ghost" />
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    maxWidth: 420,
    alignSelf: 'center',
    paddingVertical: spacing.xl,
    gap: spacing.lg,
  },
  title: {
    fontSize: typography.fontSize.xxl,
    fontWeight: typography.fontWeight.bold,
    textAlign: 'center',
  },
  body: {
    fontSize: typography.fontSize.md,
    textAlign: 'center',
    lineHeight: 22,
  },
  card: { marginBottom: spacing.xs },
  button: { marginTop: spacing.sm },
  icon: { fontSize: 48, textAlign: 'center' },
  successCard: { alignItems: 'center', gap: spacing.md },
});
