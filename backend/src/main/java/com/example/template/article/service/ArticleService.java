package com.example.template.article.service;

import com.example.template.article.dto.*;
import com.example.template.article.entity.Article;
import com.example.template.article.entity.ArticleStatus;
import com.example.template.article.mapper.ArticleMapper;
import com.example.template.article.repository.ArticleRepository;
import com.example.template.exception.ApiException;
import com.example.template.user.entity.User;
import com.example.template.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleMapper articleMapper;
    private final ArticleMetadataScraperService metadataScraper;

    @Transactional(readOnly = true)
    public List<ArticleResponse> list(String email) {
        User user = requireUser(email);
        return articleRepository.findAllByUser(user).stream()
                .sorted(Comparator
                        .comparingInt((Article a) -> a.getStatus().sortWeight())
                        .thenComparing(Article::getCreatedAt, Comparator.reverseOrder()))
                .map(articleMapper::toDto)
                .toList();
    }

    @Transactional
    public ArticleResponse create(String email, CreateArticleRequest req) {
        User user = requireUser(email);

        String title = req.title();
        String coverImageUrl = req.coverImageUrl();

        if (StringUtils.hasText(req.sourceUrl()) && (!StringUtils.hasText(title) || !StringUtils.hasText(coverImageUrl))) {
            ArticleMetadataResponse metadata = metadataScraper.scrape(req.sourceUrl());
            if (!StringUtils.hasText(title)) title = metadata.title();
            if (!StringUtils.hasText(coverImageUrl)) coverImageUrl = metadata.coverImageUrl();
        }

        if (!StringUtils.hasText(title)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Title is required (provide a title or a valid source link)");
        }

        ArticleStatus status = req.status() != null ? req.status() : ArticleStatus.TO_READ;
        LocalDate startDate = req.startDate();
        LocalDate endDate = req.endDate();

        if (status == ArticleStatus.IN_PROGRESS && startDate == null) {
            startDate = LocalDate.now();
        }
        if (status == ArticleStatus.READ) {
            if (endDate == null) endDate = LocalDate.now();
            if (startDate == null) startDate = endDate;
        }

        Article article = Article.builder()
                .user(user)
                .title(title)
                .coverImageUrl(coverImageUrl)
                .sourceUrl(req.sourceUrl())
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return articleMapper.toDto(articleRepository.save(article));
    }

    @Transactional(readOnly = true)
    public ArticleResponse get(String email, UUID id) {
        User user = requireUser(email);
        return articleMapper.toDto(requireOwned(id, user));
    }

    @Transactional
    public ArticleResponse update(String email, UUID id, UpdateArticleRequest req) {
        User user = requireUser(email);
        Article article = requireOwned(id, user);

        article.setTitle(req.title());
        article.setCoverImageUrl(req.coverImageUrl());
        article.setSourceUrl(req.sourceUrl());
        article.setStatus(req.status());
        article.setStartDate(req.startDate());
        article.setEndDate(req.endDate());

        return articleMapper.toDto(articleRepository.save(article));
    }

    @Transactional
    public ArticleResponse updateStatus(String email, UUID id, UpdateArticleStatusRequest req) {
        User user = requireUser(email);
        Article article = requireOwned(id, user);

        if (req.startDate() != null) {
            article.setStartDate(req.startDate());
        } else if (req.status() == ArticleStatus.IN_PROGRESS && article.getStartDate() == null) {
            article.setStartDate(LocalDate.now());
        }

        if (req.endDate() != null) {
            article.setEndDate(req.endDate());
        } else if (req.status() == ArticleStatus.READ && article.getEndDate() == null) {
            article.setEndDate(LocalDate.now());
        }

        if (req.status() == ArticleStatus.READ && article.getStartDate() == null) {
            article.setStartDate(article.getEndDate());
        }

        article.setStatus(req.status());
        return articleMapper.toDto(articleRepository.save(article));
    }

    @Transactional
    public void delete(String email, UUID id) {
        User user = requireUser(email);
        Article article = requireOwned(id, user);
        articleRepository.delete(article);
    }

    @Transactional
    public ArticleResponse refreshMetadata(String email, UUID id) {
        User user = requireUser(email);
        Article article = requireOwned(id, user);

        if (!StringUtils.hasText(article.getSourceUrl())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This article has no source link to refresh from");
        }

        ArticleMetadataResponse metadata = metadataScraper.scrape(article.getSourceUrl());
        if (StringUtils.hasText(metadata.title())) article.setTitle(metadata.title());
        if (StringUtils.hasText(metadata.coverImageUrl())) article.setCoverImageUrl(metadata.coverImageUrl());

        return articleMapper.toDto(articleRepository.save(article));
    }

    public ArticleMetadataResponse fetchMetadataPreview(String url) {
        return metadataScraper.scrape(url);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Article requireOwned(UUID id, User user) {
        return articleRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Article not found"));
    }
}
