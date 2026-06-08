import React, { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { authApi } from '../../lib/api/endpoints';
import { Screen } from '../../components/Screen';
import { Card } from '../../components/Card';
import { FormField } from '../../components/FormField';
import { Button } from '../../components/Button';
import { spacing, typography, useThemeColors } from '../../theme';

type Errors = { password?: string; confirmPassword?: string; general?: string };

export default function ResetPasswordScreen() {
  const { token } = useLocalSearchParams<{ token: string }>();
  const colors = useThemeColors();
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState<Errors>({});
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  function validate(): Errors {
    const e: Errors = {};
    if (!password) e.password = 'Password is required.';
    else if (password.length < 8) e.password = 'Password must be at least 8 characters.';
    if (password !== confirmPassword) e.confirmPassword = 'Passwords do not match.';
    return e;
  }

  async function handleSubmit() {
    if (!token) {
      setErrors({ general: 'Invalid reset link. Please request a new one.' });
      return;
    }
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setErrors({});
    setLoading(true);
    const result = await authApi.resetPassword({ token, newPassword: password });
    setLoading(false);
    if (result.ok) {
      setSuccess(true);
    } else {
      setErrors({ general: result.error.message });
    }
  }

  if (success) {
    return (
      <Screen scroll center>
        <View style={styles.container}>
          <Card style={styles.successCard}>
            <Text style={styles.icon}>🔒</Text>
            <Text style={[styles.title, { color: colors.text }]}>Password reset!</Text>
            <Text style={[styles.body, { color: colors.textSecondary }]}>
              Your password has been updated. You can now sign in.
            </Text>
            <Button
              label="Sign In"
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
        <Text style={[styles.title, { color: colors.text }]}>New password</Text>
        <Text style={[styles.body, { color: colors.textSecondary }]}>
          Choose a strong password for your account.
        </Text>

        <Card style={styles.card}>
          {errors.general ? (
            <Text style={[styles.generalError, { color: colors.danger }]}>
              {errors.general}
            </Text>
          ) : null}

          <FormField
            label="New password"
            value={password}
            onChangeText={setPassword}
            error={errors.password}
            secureTextEntry
            textContentType="newPassword"
            returnKeyType="next"
            placeholder="Min. 8 characters"
          />
          <FormField
            label="Confirm new password"
            value={confirmPassword}
            onChangeText={setConfirmPassword}
            error={errors.confirmPassword}
            secureTextEntry
            textContentType="newPassword"
            returnKeyType="done"
            onSubmitEditing={handleSubmit}
            placeholder="••••••••"
          />
          <Button
            label="Reset password"
            onPress={handleSubmit}
            loading={loading}
            style={styles.button}
          />
        </Card>
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
  body: { fontSize: typography.fontSize.md, textAlign: 'center' },
  card: { marginBottom: spacing.xs },
  generalError: {
    fontSize: typography.fontSize.sm,
    marginBottom: spacing.md,
    textAlign: 'center',
  },
  button: { marginTop: spacing.sm },
  icon: { fontSize: 48, textAlign: 'center' },
  successCard: { alignItems: 'center', gap: spacing.md },
});
