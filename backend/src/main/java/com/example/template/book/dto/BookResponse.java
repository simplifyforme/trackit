package com.example.template.book.dto;

import com.example.template.book.entity.BookStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String coverImageUrl,
        String sourceUrl,
        BookStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt
) {}
