import React, { useState } from 'react';
import {
  StyleSheet,
  Text,
  TextInput as RNTextInput,
  TextInputProps as RNTextInputProps,
  View,
  ViewStyle,
} from 'react-native';
import { radius, spacing, typography, useThemeColors } from '../theme';

interface TextInputProps extends RNTextInputProps {
  error?: string;
  containerStyle?: ViewStyle;
  rightElement?: React.ReactNode;
}

export function TextInput({
  error,
  containerStyle,
  rightElement,
  style,
  ...props
}: TextInputProps) {
  const colors = useThemeColors();
  const [focused, setFocused] = useState(false);

  const borderColor = error
    ? colors.danger
    : focused
    ? colors.primary
    : colors.border;

  return (
    <View style={containerStyle}>
      <View
        style={[
          styles.wrapper,
          { borderColor, backgroundColor: colors.inputBackground },
        ]}
      >
        <RNTextInput
          style={[styles.input, { color: colors.text }, style]}
          placeholderTextColor={colors.placeholder}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          autoCapitalize="none"
          autoCorrect={false}
          {...props}
        />
        {rightElement != null && (
          <View style={styles.rightElement}>{rightElement}</View>
        )}
      </View>
      {error ? (
        <Text
          style={[styles.errorText, { color: colors.danger }]}
          accessibilityRole="alert"
        >
          {error}
        </Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderRadius: radius.md,
    overflow: 'hidden',
  },
  input: {
    flex: 1,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md,
    fontSize: typography.fontSize.md,
    minHeight: 48,
  },
  rightElement: {
    paddingRight: spacing.md,
  },
  errorText: {
    fontSize: typography.fontSize.xs,
    marginTop: spacing.xs,
  },
});
