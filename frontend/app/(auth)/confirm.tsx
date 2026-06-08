import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { authApi } from '../../lib/api/endpoints';
import { Screen } from '../../components/Screen';
import { Card } from '../../components/Card';
import { Button } from '../../components/Button';
import { spacing, typography, useThemeColors } from '../../theme';

type ConfirmState = 'loading' | 'success' | 'expired' | 'invalid';

export default function ConfirmScreen() {
  const { token } = useLocalSearchParams<{ token: string }>();
  const colors = useThemeColors();
  const [state, setState] = useState<ConfirmState>('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token) {
      setState('invalid');
      setMessage('No confirmation token found in this link.');
      return;
    }

    authApi.confirm(token).then((result) => {
      if (result.ok) {
        setState('success');
        setMessage(result.data.message);
      } else {
        const msg = result.error.message.toLowerCase();
        setState(msg.includes('expired') ? 'expired' : 'invalid');
        setMessage(result.error.message);
      }
    });
  }, [token]);

  const config: Record<ConfirmState, { icon: string; title: string; color: string }> = {
    loading: { icon: '⏳', title: 'Confirming your email…', color: colors.textSecondary },
    success: { icon: '✅', title: 'Email confirmed!', color: colors.success },
    expired: { icon: '⚠️', title: 'Link expired', color: colors.danger },
    invalid: { icon: '❌', title: 'Invalid link', color: colors.danger },
  };

  const { icon, title, color } = config[state];

  return (
    <Screen scroll center>
      <View style={styles.container}>
        <Card style={styles.card}>
          <Text style={styles.icon}>{icon}</Text>
          <Text style={[styles.title, { color }]}>{title}</Text>
          {message ? (
            <Text style={[styles.message, { color: colors.textSecondary }]}>{message}</Text>
          ) : null}
          {state !== 'loading' && (
            <Button
              label={state === 'success' ? 'Sign In' : 'Back to Login'}
              onPress={() => router.replace('/(auth)/login')}
              style={styles.button}
            />
          )}
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
  },
  card: { alignItems: 'center', gap: spacing.md },
  icon: { fontSize: 56, textAlign: 'center' },
  title: {
    fontSize: typography.fontSize.xl,
    fontWeight: typography.fontWeight.bold,
    textAlign: 'center',
  },
  message: {
    textAlign: 'center',
    fontSize: typography.fontSize.md,
    lineHeight: 22,
  },
  button: { width: '100%', marginTop: spacing.sm },
});
