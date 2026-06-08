package com.example.template.settings.controller;

import com.example.template.settings.dto.SettingsRequest;
import com.example.template.settings.dto.SettingsResponse;
import com.example.template.settings.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public SettingsResponse get(@AuthenticationPrincipal UserDetails userDetails) {
        return settingsService.get(userDetails.getUsername());
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public SettingsResponse save(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SettingsRequest request) {
        return settingsService.save(userDetails.getUsername(), request);
    }
}
