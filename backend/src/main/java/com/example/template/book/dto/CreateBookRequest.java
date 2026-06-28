package com.example.template.book.dto;

import com.example.template.book.entity.BookStatus;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

/**
 * Either title/coverImageUrl or sourceUrl (or both) must be provided.
 * When sourceUrl is given and title/coverImageUrl are not, they are auto-fetched from the page.
 */
public record CreateBookRequest(

        @Size(max = 255)
        String title,

        String coverImageUrl,

        @URL
        String sourceUrl,

        BookStatus status,

        LocalDate startDate,

        LocalDate endDate
) {}
