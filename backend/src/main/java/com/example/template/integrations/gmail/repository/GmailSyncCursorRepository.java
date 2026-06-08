package com.example.template.integrations.gmail.repository;

import com.example.template.integrations.gmail.entity.GmailSyncCursor;
import com.example.template.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GmailSyncCursorRepository extends JpaRepository<GmailSyncCursor, UUID> {

    Optional<GmailSyncCursor> findByUser(User user);
}
