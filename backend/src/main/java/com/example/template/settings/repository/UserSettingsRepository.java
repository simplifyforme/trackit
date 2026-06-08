package com.example.template.settings.repository;

import com.example.template.settings.entity.UserSettings;
import com.example.template.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSettingsRepository extends JpaRepository<UserSettings, UUID> {

    Optional<UserSettings> findByUser(User user);
}
