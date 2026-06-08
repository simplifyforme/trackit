import React, { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Link, router } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import { Screen } from '../../components/Screen';
import { Card } from '../../components/Card';
import { FormField } from '../../components/FormField';
import { Button } from '../../components/Button';
import { spacing, typography, useThemeColors } from '../../theme';

type Errors = {
  email?: string;
  password?: string;
  confirmPassword?: string;
  general?: string;
};

export default function RegisterScreen() {
  const { register } = useAuth();
  const colors = useThemeColors();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState<Errors>({});
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  function validate(): Errors {
    const e: Errors = {};
    if (!email.trim()) e.email = 'Email is required.';
    else if (!/\S+@\S+\.\S+/.test(email)) e.email = 'Enter a valid email.';
    if (!password) e.password = 'Password is required.';
    else if (password.length < 8) e.password = 'Password must be at least 8 characters.';
    if (password !== confirmPassword) e.confirmPassword = 'Passwords do not match.';
    return e;
  }

  async function handleRegister() {
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setErrors({});
    setLoading(true);
    const result = await register(email.trim().toLowerCase(), password);
    setLoading(false);
    if (result.error) {
      setErrors({ general: result.error });
    } else {
      setSuccessMessage(result.message ?? 'Check your email to confirm your account.');
    }
  }

  if (successMessage) {
    return (
      <Screen scroll center>
        <View style={styles.container}>
          <Card style={styles.successCard}>
            <Text style={styles.icon}>✉️</Text>
            <Text style={[styles.title, { color: colors.text }]}>Check your email</Text>
            <Text style={[styles.body, { color: colors.textSecondary }]}>
              {successMessage}
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
        <Text style={[styles.title, { color: colors.text }]}>Create account</Text>
        <Text style={[styles.subtitle, { color: colors.textSecondary }]}>
          Sign up to get started
        </Text>

        <Card style={styles.card}>
          {errors.general ? (
            <Text style={[styles.generalError, { color: colors.danger }]}>
              {errors.general}
            </Text>
          ) : null}

          <FormField
            label="Email"
            value={email}
            onChangeText={setEmail}
            error={errors.email}
            keyboardType="email-address"
            textContentType="emailAddress"
            returnKeyType="next"
            placeholder="you@example.com"
          />
          <FormField
            label="Password"
            value={password}
            onChangeText={setPassword}
            error={errors.password}
            secureTextEntry
            textContentType="newPassword"
            returnKeyType="next"
            placeholder="Min. 8 characters"
          />
          <FormField
            label="Confirm password"
            value={confirmPassword}
            onChangeText={setConfirmPassword}
            error={errors.confirmPassword}
            secureTextEntry
            textContentType="newPassword"
            returnKeyType="done"
            onSubmitEditing={handleRegister}
            placeholder="••••••••"
          />

          <Button
            label="Create Account"
            onPress={handleRegister}
            loading={loading}
            style={styles.button}
          />
        </Card>

        <View style={styles.footer}>
          <Text style={{ color: colors.textSecondary }}>Already have an account? </Text>
          <Link href="/(auth)/login" asChild>
            <Pressable>
              <Text style={[styles.link, { color: colors.primary }]}>Sign in</Text>
            </Pressable>
          </Link>
        </View>
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
  },
  title: {
    fontSize: typography.fontSize.xxxl,
    fontWeight: typography.fontWeight.bold,
    textAlign: 'center',
    marginBottom: spacing.xs,
  },
  subtitle: {
    fontSize: typography.fontSize.md,
    textAlign: 'center',
    marginBottom: spacing.xl,
  },
  card: { marginBottom: spacing.lg },
  generalError: {
    fontSize: typography.fontSize.sm,
    marginBottom: spacing.md,
    textAlign: 'center',
  },
  button: { marginTop: spacing.sm },
  footer: { flexDirection: 'row', justifyContent: 'center' },
  link: { fontWeight: typography.fontWeight.semibold },
  successCard: { alignItems: 'center', gap: spacing.md },
  icon: { fontSize: 48, textAlign: 'center' },
  body: { textAlign: 'center', fontSize: typography.fontSize.md, lineHeight: 24 },
});
