import React from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  View,
  ViewStyle,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useThemeColors } from '../theme';

interface ScreenProps {
  children: React.ReactNode;
  scroll?: boolean;
  center?: boolean;
  padded?: boolean;
  style?: ViewStyle;
}

export function Screen({
  children,
  scroll = true,
  center = false,
  padded = true,
  style,
}: ScreenProps) {
  const colors = useThemeColors();

  const inner = (
    <View style={[styles.inner, center && styles.center, padded && styles.padded, style]}>
      {children}
    </View>
  );

  return (
    <SafeAreaView style={[styles.safe, { backgroundColor: colors.background }]}>
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        {scroll ? (
          <ScrollView
            contentContainerStyle={[styles.scrollContent, center && styles.scrollCenter]}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            {inner}
          </ScrollView>
        ) : (
          inner
        )}
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1 },
  flex: { flex: 1 },
  scrollContent: { flexGrow: 1 },
  scrollCenter: { justifyContent: 'center' },
  inner: { flex: 1 },
  center: { alignItems: 'center', justifyContent: 'center' },
  padded: { padding: 16 },
});
