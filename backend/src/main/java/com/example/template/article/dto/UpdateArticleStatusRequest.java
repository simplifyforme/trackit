package com.example.template.article.dto;

import com.example.template.article.entity.ArticleStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * startDate/endDate are optional overrides; when omitted, the service stamps
 * "today" on the relevant date as the status transitions (e.g. marking READ stamps endDate).
 */
public record UpdateArticleStatusRequest(

        @NotNull
        ArticleStatus status,

        LocalDate startDate,

        LocalDate endDate
) {}
