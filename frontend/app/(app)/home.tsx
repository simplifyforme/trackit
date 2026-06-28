import React, { useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';
import { router, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '../../contexts/AuthContext';
import { Screen } from '../../components/Screen';
import { Card } from '../../components/Card';
import { spacing, typography, useThemeColors, type ThemeColors } from '../../theme';

export default function HomeScreen() {
  const { user, logout } = useAuth();
  const colors = useThemeColors();
  const [logoutLoading, setLogoutLoading] = useState(false);

  const username = user?.email?.split('@')[0] ?? null;

  async function handleLogout() {
    setLogoutLoading(true);
    await logout();
  }

  return (
    <Screen scroll={false} padded>
      <Stack.Screen
        options={{
          title: 'Home',
          headerShown: true,
          headerRight: () => (
            <View style={styles.headerRight}>
              <Pressable
                onPress={() => router.push('/(app)/profile')}
                style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
                accessibilityLabel="My Profile"
                accessibilityRole="button"
              >
                <Ionicons name="person-outline" size={24} color={colors.text} />
              </Pressable>

              <Pressable
                onPress={handleLogout}
                disabled={logoutLoading}
                style={({ pressed }) => [
                  styles.iconButton,
                  (pressed || logoutLoading) && styles.pressed,
                ]}
                accessibilityLabel="Sign Out"
                accessibilityRole="button"
              >
                {logoutLoading ? (
                  <ActivityIndicator size="small" color={colors.text} />
                ) : (
                  <Ionicons name="log-out-outline" size={24} color={colors.text} />
                )}
              </Pressable>
            </View>
          ),
        }}
      />

      <View style={styles.container}>
        <Text style={[styles.label, { color: colors.textSecondary }]}>Welcome,</Text>
        {username == null ? (
          <ActivityIndicator color={colors.primary} style={styles.loader} />
        ) : (
          <Text
            style={[styles.username, { color: colors.text }]}
            numberOfLines={1}
            adjustsFontSizeToFit
          >
            {username}
          </Text>
        )}

        <View style={styles.grid}>
          <NavCard icon="checkmark-done-outline" label="To-Do" route="/(app)/todos" colors={colors} />
          <NavCard icon="bag-outline" label="Orders" route="/(app)/orders" colors={colors} />
          <NavCard icon="heart-outline" label="Wishlist" route="/(app)/wishlist" colors={colors} />
          <NavCard icon="book-outline" label="Books" route="/(app)/books" colors={colors} />
          <NavCard icon="newspaper-outline" label="Articles" route="/(app)/articles" colors={colors} />
          <NavCard icon="settings-outline" label="Settings" route="/(app)/settings" colors={colors} />
        </View>
      </View>
    </Screen>
  );
}

function NavCard({ icon, label, route, colors }: {
  icon: React.ComponentProps<typeof Ionicons>['name'];
  label: string;
  route: string;
  colors: ThemeColors;
}) {
  return (
    <Pressable
      onPress={() => router.push(route as any)}
      style={({ pressed }) => [styles.navCard, pressed && styles.pressed]}
    >
      <Card style={styles.navCardInner}>
        <Ionicons name={icon} size={28} color={colors.primary} />
        <Text style={[styles.navLabel, { color: colors.text }]}>{label}</Text>
      </Card>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  headerRight: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    marginRight: spacing.xs,
  },
  iconButton: {
    padding: spacing.sm,
    borderRadius: 8,
  },
  pressed: { opacity: 0.5 },
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  label: { fontSize: typography.fontSize.xl, marginBottom: spacing.sm },
  username: {
    fontSize: typography.fontSize.xxxl,
    fontWeight: typography.fontWeight.bold,
    textAlign: 'center',
  },
  loader: { marginTop: spacing.sm },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.md,
    marginTop: spacing.xl,
    justifyContent: 'center',
  },
  navCard: { width: 120 },
  navCardInner: {
    alignItems: 'center',
    gap: spacing.sm,
    padding: spacing.md,
  },
  navLabel: { fontSize: typography.fontSize.sm, fontWeight: '500' },
});
