import { useColorScheme } from 'react-native';

export const palette = {
  primary: '#4F6EF7',
  primaryDark: '#3A56D4',
  danger: '#E53E3E',
  success: '#38A169',

  white: '#FFFFFF',
  black: '#000000',

  gray50: '#F9FAFB',
  gray100: '#F3F4F6',
  gray200: '#E5E7EB',
  gray300: '#D1D5DB',
  gray400: '#9CA3AF',
  gray500: '#6B7280',
  gray600: '#4B5563',
  gray700: '#374151',
  gray800: '#1F2937',
  gray900: '#111827',
};

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

export const typography = {
  fontSize: {
    xs: 12,
    sm: 14,
    md: 16,
    lg: 18,
    xl: 20,
    xxl: 24,
    xxxl: 30,
  },
  fontWeight: {
    regular: '400' as const,
    medium: '500' as const,
    semibold: '600' as const,
    bold: '700' as const,
  },
};

export const radius = {
  sm: 6,
  md: 10,
  lg: 16,
  full: 9999,
};

export const lightColors = {
  background: palette.gray50,
  surface: palette.white,
  border: palette.gray200,
  text: palette.gray900,
  textSecondary: palette.gray500,
  placeholder: palette.gray400,
  inputBackground: palette.white,
  primary: palette.primary,
  primaryText: palette.white,
  danger: palette.danger,
  success: palette.success,
};

export const darkColors: typeof lightColors = {
  background: palette.gray900,
  surface: palette.gray800,
  border: palette.gray700,
  text: palette.gray50,
  textSecondary: palette.gray400,
  placeholder: palette.gray500,
  inputBackground: palette.gray700,
  primary: palette.primary,
  primaryText: palette.white,
  danger: palette.danger,
  success: palette.success,
};

export type ThemeColors = typeof lightColors;

export function useThemeColors(): ThemeColors {
  const scheme = useColorScheme();
  return scheme === 'dark' ? darkColors : lightColors;
}
