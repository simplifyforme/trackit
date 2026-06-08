import React, { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Linking,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { Stack } from 'expo-router';
import { gmailApi, settingsApi } from '../../../lib/api/endpoints';
import { Screen } from '../../../components/Screen';
import { Card } from '../../../components/Card';
import { Button } from '../../../components/Button';
import { FormField } from '../../../components/FormField';
import { spacing, typography, useThemeColors } from '../../../theme';

export default function SettingsScreen() {
  const colors = useThemeColors();
  const [apiKey, setApiKey] = useState('');
  const [model, setModel] = useState('openrouter/auto');
  const [keyConfigured, setKeyConfigured] = useState(false);
  const [gmailConnected, setGmailConnected] = useState(false);
  const [loadingSettings, setLoadingSettings] = useState(true);
  const [savingSettings, setSavingSettings] = useState(false);
  const [connectingGmail, setConnectingGmail] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    Promise.all([settingsApi.get(), gmailApi.status()]).then(([sr, gr]) => {
      if (sr.ok) {
        setKeyConfigured(sr.data.openrouterApiKeyConfigured);
        setModel(sr.data.openrouterModel);
      }
      if (gr.ok) setGmailConnected(gr.data.connected);
      setLoadingSettings(false);
    });
  }, []);

  async function handleSaveSettings() {
    setSavingSettings(true);
    setError('');
    setSuccess('');
    const result = await settingsApi.save({
      openrouterApiKey: apiKey.trim() || undefined,
      openrouterModel: model.trim() || undefined,
    });
    setSavingSettings(false);
    if (result.ok) {
      setKeyConfigured(result.data.openrouterApiKeyConfigured);
      setApiKey('');
      setSuccess('Settings saved.');
    } else {
      setError(result.error.message ?? 'Failed to save.');
    }
  }

  async function handleConnectGmail() {
    setConnectingGmail(true);
    setError('');
    const result = await gmailApi.connect();
    setConnectingGmail(false);
    if (result.ok) {
      await Linking.openURL(result.data.authorizationUrl);
    } else {
      setError(result.error.message ?? 'Failed to initiate Gmail connection.');
    }
  }

  async function handleDisconnectGmail() {
    const result = await gmailApi.disconnect();
    if (result.ok) setGmailConnected(false);
  }

  if (loadingSettings) {
    return (
      <Screen scroll={false} padded>
        <Stack.Screen options={{ title: 'Settings' }} />
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      </Screen>
    );
  }

  return (
    <Screen scroll padded>
      <Stack.Screen options={{ title: 'Settings' }} />
      <ScrollView contentContainerStyle={styles.container}>

        {/* OpenRouter */}
        <Text style={[styles.section, { color: colors.textSecondary }]}>AI / OPENROUTER</Text>
        <Card style={styles.card}>
          <Text style={[styles.statusText, { color: keyConfigured ? colors.success : colors.textSecondary }]}>
            API key: {keyConfigured ? 'Configured ✓' : 'Not configured'}
          </Text>
          <FormField
            label="Paste new API key (write-only)"
            value={apiKey}
            onChangeText={setApiKey}
            placeholder="sk-or-…"
            autoCapitalize="none"
            secureTextEntry
          />
          <FormField
            label="Model"
            value={model}
            onChangeText={setModel}
            placeholder="openrouter/auto"
            autoCapitalize="none"
          />
          {error ? <Text style={[styles.msg, { color: colors.danger }]}>{error}</Text> : null}
          {success ? <Text style={[styles.msg, { color: colors.success }]}>{success}</Text> : null}
          <Button label="Save Settings" onPress={handleSaveSettings} loading={savingSettings} style={styles.btn} />
        </Card>

        {/* Gmail */}
        <Text style={[styles.section, { color: colors.textSecondary }]}>GMAIL INTEGRATION</Text>
        <Card style={styles.card}>
          <Text style={[styles.statusText, { color: gmailConnected ? colors.success : colors.textSecondary }]}>
            {gmailConnected ? 'Gmail connected ✓' : 'No Gmail account connected'}
          </Text>
          <Text style={[styles.hint, { color: colors.textSecondary }]}>
            Connecting Gmail allows the app to automatically detect and track orders from your inbox.
            This is read-only access (gmail.readonly scope) and is separate from your login.
          </Text>
          {gmailConnected ? (
            <Button label="Disconnect Gmail" onPress={handleDisconnectGmail} variant="ghost" style={styles.btn} />
          ) : (
            <Button
              label="Connect Gmail"
              onPress={handleConnectGmail}
              loading={connectingGmail}
              style={styles.btn}
            />
          )}
        </Card>

      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: { gap: spacing.md },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  section: {
    fontSize: typography.fontSize.xs,
    fontWeight: '600',
    letterSpacing: 0.8,
    textTransform: 'uppercase',
    marginTop: spacing.sm,
  },
  card: { gap: spacing.sm },
  statusText: { fontSize: typography.fontSize.sm, fontWeight: '500' },
  hint: { fontSize: typography.fontSize.xs, lineHeight: 18 },
  msg: { fontSize: typography.fontSize.sm },
  btn: { marginTop: spacing.xs },
});
