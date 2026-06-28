package com.example.template.book.dto;

import com.example.template.book.entity.BookStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public record UpdateBookRequest(

        @NotBlank
        @Size(max = 255)
        String title,

        String coverImageUrl,

        @URL
        String sourceUrl,

        @NotNull
        BookStatus status,

        LocalDate startDate,

        LocalDate endDate
) {}
