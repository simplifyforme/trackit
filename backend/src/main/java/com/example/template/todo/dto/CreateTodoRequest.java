package com.example.template.todo.dto;

import com.example.template.todo.entity.ImportanceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateTodoRequest(

        @NotBlank
        @Size(max = 255)
        String title,

        String description,

        ImportanceLevel importance,

        Instant deadline
) {}
