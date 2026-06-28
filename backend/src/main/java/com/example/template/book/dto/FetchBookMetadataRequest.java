package com.example.template.book.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record FetchBookMetadataRequest(

        @NotBlank
        @URL
        String url
) {}
