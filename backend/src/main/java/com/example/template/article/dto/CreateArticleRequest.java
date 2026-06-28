package com.example.template.article.dto;

import com.example.template.article.entity.ArticleStatus;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

/**
 * Either title/coverImageUrl or sourceUrl (or both) must be provided.
 * When sourceUrl is given and title/coverImageUrl are not, they are auto-fetched from the page.
 */
public record CreateArticleRequest(

        @Size(max = 255)
        String title,

        String coverImageUrl,

        @URL
        String sourceUrl,

        ArticleStatus status,

        LocalDate startDate,

        LocalDate endDate
) {}
