package com.example.template.settings.dto;

import jakarta.validation.constraints.Size;

public record SettingsRequest(

        /** Plain-text API key; stored encrypted, never echoed back. Null = leave unchanged. */
        String openrouterApiKey,

        @Size(max = 255)
        String openrouterModel
) {}
