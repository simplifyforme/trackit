package com.example.template.todo.dto;

import com.example.template.todo.entity.ImportanceLevel;

import java.time.Instant;
import java.util.UUID;

public record TodoResponse(
        UUID id,
        String title,
        String description,
        ImportanceLevel importance,
        Instant deadline,
        boolean isDone,
        Instant createdAt,
        Instant updatedAt
) {}
