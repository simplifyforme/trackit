package com.example.template.article.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record FetchArticleMetadataRequest(

        @NotBlank
        @URL
        String url
) {}
