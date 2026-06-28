package com.example.template.book.dto;

import com.example.template.book.entity.BookStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * startDate/endDate are optional overrides; when omitted, the service stamps
 * "today" on the relevant date as the status transitions (e.g. marking READ stamps endDate).
 */
public record UpdateBookStatusRequest(

        @NotNull
        BookStatus status,

        LocalDate startDate,

        LocalDate endDate
) {}
