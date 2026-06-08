import React from 'react';
import { StyleSheet, Text, View, ViewStyle } from 'react-native';
import { TextInput } from './TextInput';
import { spacing, typography, useThemeColors } from '../theme';

type TextInputProps = React.ComponentProps<typeof TextInput>;

interface FormFieldProps extends Omit<TextInputProps, 'containerStyle'> {
  label: string;
  containerStyle?: ViewStyle;
}

export function FormField({ label, containerStyle, ...inputProps }: FormFieldProps) {
  const colors = useThemeColors();
  return (
    <View style={[styles.container, containerStyle]}>
      <Text style={[styles.label, { color: colors.text }]}>{label}</Text>
      <TextInput {...inputProps} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: spacing.md,
  },
  label: {
    fontSize: typography.fontSize.sm,
    fontWeight: typography.fontWeight.medium,
    marginBottom: spacing.xs,
  },
});
