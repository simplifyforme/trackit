package com.example.template.article.controller;

import com.example.template.article.dto.*;
import com.example.template.article.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<ArticleResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
        return articleService.list(userDetails.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ArticleResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateArticleRequest request) {
        return articleService.create(userDetails.getUsername(), request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ArticleResponse get(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return articleService.get(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ArticleResponse update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateArticleRequest request) {
        return articleService.update(userDetails.getUsername(), id, request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ArticleResponse updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateArticleStatusRequest request) {
        return articleService.updateStatus(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        articleService.delete(userDetails.getUsername(), id);
    }

    @PostMapping("/{id}/refresh-metadata")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ArticleResponse refreshMetadata(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return articleService.refreshMetadata(userDetails.getUsername(), id);
    }

    @PostMapping("/metadata-preview")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ArticleMetadataResponse metadataPreview(@Valid @RequestBody FetchArticleMetadataRequest request) {
        return articleService.fetchMetadataPreview(request.url());
    }
}
