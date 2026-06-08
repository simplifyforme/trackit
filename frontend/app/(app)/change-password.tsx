import React, { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { router, Stack } from 'expo-router';
import { authApi } from '../../lib/api/endpoints';
import { Screen } from '../../components/Screen';
import { Card } from '../../components/Card';
import { FormField } from '../../components/FormField';
import { Button } from '../../components/Button';
import { spacing, typography, useThemeColors } from '../../theme';

type Errors = {
  currentPassword?: string;
  newPassword?: string;
  confirmPassword?: string;
  general?: string;
};

export default function ChangePasswordScreen() {
  const colors = useThemeColors();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState<Errors>({});
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  function validate(): Errors {
    const e: Errors = {};
    if (!currentPassword) e.currentPassword = 'Current password is required.';
    if (!newPassword) e.newPassword = 'New password is required.';
    else if (newPassword.length < 8) e.newPassword = 'Password must be at least 8 characters.';
    else if (newPassword === currentPassword)
      e.newPassword = 'New password must differ from current.';
    if (newPassword !== confirmPassword) e.confirmPassword = 'Passwords do not match.';
    return e;
  }

  async function handleSubmit() {
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setErrors({});
    setLoading(true);
    const result = await authApi.changePassword({ currentPassword, newPassword });
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
        <Stack.Screen options={{ title: 'Change Password' }} />
        <View style={styles.container}>
          <Card style={styles.successCard}>
            <Text style={styles.icon}>🔒</Text>
            <Text style={[styles.title, { color: colors.text }]}>Password updated</Text>
            <Text style={[styles.body, { color: colors.textSecondary }]}>
              Your password has been changed successfully.
            </Text>
            <Button label="Done" onPress={() => router.back()} style={styles.button} />
          </Card>
        </View>
      </Screen>
    );
  }

  return (
    <Screen scroll center>
      <Stack.Screen options={{ title: 'Change Password' }} />
      <View style={styles.container}>
        <Text style={[styles.title, { color: colors.text }]}>Change password</Text>

        <Card style={styles.card}>
          {errors.general ? (
            <Text style={[styles.generalError, { color: colors.danger }]}>
              {errors.general}
            </Text>
          ) : null}

          <FormField
            label="Current password"
            value={currentPassword}
            onChangeText={setCurrentPassword}
            error={errors.currentPassword}
            secureTextEntry
            textContentType="password"
            returnKeyType="next"
            placeholder="••••••••"
          />
          <FormField
            label="New password"
            value={newPassword}
            onChangeText={setNewPassword}
            error={errors.newPassword}
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
            label="Update password"
            onPress={handleSubmit}
            loading={loading}
            style={styles.button}
          />
        </Card>

        <Button label="Cancel" onPress={() => router.back()} variant="ghost" />
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
  card: { marginBottom: spacing.xs },
  generalError: {
    fontSize: typography.fontSize.sm,
    marginBottom: spacing.md,
    textAlign: 'center',
  },
  button: { marginTop: spacing.sm },
  icon: { fontSize: 48, textAlign: 'center' },
  successCard: { alignItems: 'center', gap: spacing.md },
  body: { textAlign: 'center', fontSize: typography.fontSize.md, lineHeight: 22 },
});
