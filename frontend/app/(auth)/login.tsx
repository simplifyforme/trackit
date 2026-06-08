import React, { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Link, router } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import { Screen } from '../../components/Screen';
import { Card } from '../../components/Card';
import { FormField } from '../../components/FormField';
import { Button } from '../../components/Button';
import { spacing, typography, useThemeColors } from '../../theme';

type Errors = { email?: string; password?: string; general?: string };

export default function LoginScreen() {
  const { login } = useAuth();
  const colors = useThemeColors();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<Errors>({});
  const [loading, setLoading] = useState(false);

  function validate(): Errors {
    const e: Errors = {};
    if (!email.trim()) e.email = 'Email is required.';
    else if (!/\S+@\S+\.\S+/.test(email)) e.email = 'Enter a valid email.';
    if (!password) e.password = 'Password is required.';
    return e;
  }

  async function handleLogin() {
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setErrors({});
    setLoading(true);
    const result = await login(email.trim().toLowerCase(), password);
    setLoading(false);
    if (result.error) {
      setErrors({ general: result.error });
    } else {
      router.replace('/(app)/home');
    }
  }

  return (
    <Screen scroll center>
      <View style={styles.container}>
        <Text style={[styles.title, { color: colors.text }]}>Welcome back</Text>
        <Text style={[styles.subtitle, { color: colors.textSecondary }]}>
          Sign in to your account
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
            textContentType="password"
            returnKeyType="done"
            onSubmitEditing={handleLogin}
            placeholder="••••••••"
          />

          <Link href="/(auth)/forgot-password" asChild>
            <Pressable style={styles.forgotLink}>
              <Text style={[styles.link, { color: colors.primary }]}>Forgot password?</Text>
            </Pressable>
          </Link>

          <Button
            label="Sign In"
            onPress={handleLogin}
            loading={loading}
            style={styles.button}
          />
        </Card>

        <View style={styles.footer}>
          <Text style={{ color: colors.textSecondary }}>Don't have an account? </Text>
          <Link href="/(auth)/register" asChild>
            <Pressable>
              <Text style={[styles.link, { color: colors.primary }]}>Register</Text>
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
  forgotLink: {
    alignSelf: 'flex-end',
    marginBottom: spacing.lg,
    marginTop: -spacing.sm,
  },
  button: { marginTop: spacing.sm },
  footer: { flexDirection: 'row', justifyContent: 'center' },
  link: { fontWeight: typography.fontWeight.semibold },
});
