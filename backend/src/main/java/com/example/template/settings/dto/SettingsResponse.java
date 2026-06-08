package com.example.template.settings.dto;

public record SettingsResponse(
        /** true when an API key has been saved; the key itself is never returned. */
        boolean openrouterApiKeyConfigured,
        String openrouterModel
) {}
