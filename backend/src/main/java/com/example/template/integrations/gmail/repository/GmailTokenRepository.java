package com.example.template.integrations.gmail.repository;

import com.example.template.integrations.gmail.entity.GmailToken;
import com.example.template.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GmailTokenRepository extends JpaRepository<GmailToken, UUID> {

    Optional<GmailToken> findByUser(User user);

    /** Returns all users who have connected a Gmail account, for polling. */
    List<GmailToken> findAll();
}
