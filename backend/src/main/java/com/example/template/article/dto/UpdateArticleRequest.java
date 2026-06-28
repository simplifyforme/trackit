package com.example.template.article.dto;

import com.example.template.article.entity.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public record UpdateArticleRequest(

        @NotBlank
        @Size(max = 255)
        String title,

        String coverImageUrl,

        @URL
        String sourceUrl,

        @NotNull
        ArticleStatus status,

        LocalDate startDate,

        LocalDate endDate
) {}
