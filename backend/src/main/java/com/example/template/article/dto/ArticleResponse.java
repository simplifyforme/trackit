package com.example.template.article.dto;

import com.example.template.article.entity.ArticleStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ArticleResponse(
        UUID id,
        String title,
        String coverImageUrl,
        String sourceUrl,
        ArticleStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt
) {}
