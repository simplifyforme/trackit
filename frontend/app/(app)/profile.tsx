import React, { useEffect, useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { router, Stack } from 'expo-router';
import { userApi } from '../../lib/api/endpoints';
import { useAuth } from '../../contexts/AuthContext';
import { Screen } from '../../components/Screen';
import { Card } from '../../components/Card';
import { Button } from '../../components/Button';
import { spacing, typography, useThemeColors } from '../../theme';
import type { UserResponse } from '../../types/api';

export default function ProfileScreen() {
  const { user: contextUser } = useAuth();
  const colors = useThemeColors();

  const [user, setUser] = useState<UserResponse | null>(contextUser);
  const [loading, setLoading] = useState(!contextUser);
  const [error, setError] = useState('');

  useEffect(() => {
    userApi.me().then((result) => {
      if (result.ok) {
        setUser(result.data);
      } else {
        setError('Could not load profile.');
      }
      setLoading(false);
    });
  }, []);

  return (
    <Screen scroll padded>
      <Stack.Screen options={{ title: 'My Profile' }} />

      {loading && !user ? (
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : error ? (
        <View style={styles.center}>
          <Text style={[styles.errorText, { color: colors.danger }]}>{error}</Text>
          <Button label="Go Back" onPress={() => router.back()} variant="ghost" style={styles.backButton} />
        </View>
      ) : (
        <View style={styles.container}>
          <Card>
            <Row label="Email" value={user?.email} colors={colors} />
            <Row
              label="Roles"
              value={user?.roles?.join(', ')}
              colors={colors}
            />
            <Row
              label="Account"
              value={user == null ? undefined : user.enabled ? 'Active' : 'Inactive'}
              colors={colors}
              valueColor={user?.enabled ? colors.success : colors.danger}
            />
          </Card>

          <Button
            label="Change Password"
            onPress={() => router.push('/(app)/change-password')}
            variant="ghost"
            style={styles.changePassword}
          />
        </View>
      )}
    </Screen>
  );
}

function Row({
  label,
  value,
  colors,
  valueColor,
}: {
  label: string;
  value: string | undefined;
  colors: ReturnType<typeof useThemeColors>;
  valueColor?: string;
}) {
  return (
    <View style={[styles.row, { borderTopColor: colors.border }]}>
      <Text style={[styles.rowLabel, { color: colors.textSecondary }]}>{label}</Text>
      <Text
        style={[styles.rowValue, { color: valueColor ?? colors.text }]}
        numberOfLines={1}
      >
        {value ?? '…'}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md },
  container: { gap: spacing.lg },
  errorText: { fontSize: typography.fontSize.md },
  backButton: { marginTop: spacing.sm },
  changePassword: { marginTop: spacing.xs },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: spacing.md,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  rowLabel: { fontSize: typography.fontSize.sm },
  rowValue: {
    fontSize: typography.fontSize.sm,
    fontWeight: typography.fontWeight.medium,
    flex: 1,
    textAlign: 'right',
  },
});
