package com.example.template.settings.service;

import com.example.template.config.EncryptionService;
import com.example.template.exception.ApiException;
import com.example.template.settings.dto.SettingsRequest;
import com.example.template.settings.dto.SettingsResponse;
import com.example.template.settings.entity.UserSettings;
import com.example.template.settings.repository.UserSettingsRepository;
import com.example.template.user.entity.User;
import com.example.template.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserSettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    @Transactional(readOnly = true)
    public SettingsResponse get(String email) {
        User user = requireUser(email);
        return settingsRepository.findByUser(user)
                .map(s -> new SettingsResponse(
                        s.getOpenrouterApiKey() != null,
                        s.getOpenrouterModel()))
                .orElse(new SettingsResponse(false, "openrouter/auto"));
    }

    @Transactional
    public SettingsResponse save(String email, SettingsRequest req) {
        User user = requireUser(email);
        UserSettings settings = settingsRepository.findByUser(user)
                .orElseGet(() -> UserSettings.builder().user(user).build());

        if (req.openrouterApiKey() != null && !req.openrouterApiKey().isBlank()) {
            settings.setOpenrouterApiKey(encryptionService.encrypt(req.openrouterApiKey()));
        }
        if (req.openrouterModel() != null && !req.openrouterModel().isBlank()) {
            settings.setOpenrouterModel(req.openrouterModel());
        }
        settingsRepository.save(settings);
        return new SettingsResponse(settings.getOpenrouterApiKey() != null, settings.getOpenrouterModel());
    }

    /** Resolves the effective API key: user setting takes priority over env var. */
    @Transactional(readOnly = true)
    public String resolveApiKey(User user, String envFallback) {
        return settingsRepository.findByUser(user)
                .map(UserSettings::getOpenrouterApiKey)
                .filter(k -> k != null && !k.isBlank())
                .map(encryptionService::decrypt)
                .filter(k -> !k.isBlank())
                .orElse(envFallback);
    }

    /** Resolves the model: user setting takes priority over the default. */
    @Transactional(readOnly = true)
    public String resolveModel(User user, String defaultModel) {
        return settingsRepository.findByUser(user)
                .map(UserSettings::getOpenrouterModel)
                .filter(m -> m != null && !m.isBlank())
                .orElse(defaultModel);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
