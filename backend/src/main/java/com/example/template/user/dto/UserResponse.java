package com.example.template.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {

    private final UUID id;
    private final String email;
    private final boolean enabled;
    private final Set<String> roles;
    private final Instant createdAt;
}
