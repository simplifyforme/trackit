package com.example.template.book.service;

import com.example.template.book.dto.*;
import com.example.template.book.entity.Book;
import com.example.template.book.entity.BookStatus;
import com.example.template.book.mapper.BookMapper;
import com.example.template.book.repository.BookRepository;
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
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookMapper bookMapper;
    private final BookMetadataScraperService metadataScraper;

    @Transactional(readOnly = true)
    public List<BookResponse> list(String email) {
        User user = requireUser(email);
        return bookRepository.findAllByUser(user).stream()
                .sorted(Comparator
                        .comparingInt((Book b) -> b.getStatus().sortWeight())
                        .thenComparing(Book::getCreatedAt, Comparator.reverseOrder()))
                .map(bookMapper::toDto)
                .toList();
    }

    @Transactional
    public BookResponse create(String email, CreateBookRequest req) {
        User user = requireUser(email);

        String title = req.title();
        String coverImageUrl = req.coverImageUrl();

        if (StringUtils.hasText(req.sourceUrl()) && (!StringUtils.hasText(title) || !StringUtils.hasText(coverImageUrl))) {
            BookMetadataResponse metadata = metadataScraper.scrape(req.sourceUrl());
            if (!StringUtils.hasText(title)) title = metadata.title();
            if (!StringUtils.hasText(coverImageUrl)) coverImageUrl = metadata.coverImageUrl();
        }

        if (!StringUtils.hasText(title)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Title is required (provide a title or a valid source link)");
        }

        BookStatus status = req.status() != null ? req.status() : BookStatus.TO_READ;
        LocalDate startDate = req.startDate();
        LocalDate endDate = req.endDate();

        if (status == BookStatus.IN_PROGRESS && startDate == null) {
            startDate = LocalDate.now();
        }
        if (status == BookStatus.READ) {
            if (endDate == null) endDate = LocalDate.now();
            if (startDate == null) startDate = endDate;
        }

        Book book = Book.builder()
                .user(user)
                .title(title)
                .coverImageUrl(coverImageUrl)
                .sourceUrl(req.sourceUrl())
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return bookMapper.toDto(bookRepository.save(book));
    }

    @Transactional(readOnly = true)
    public BookResponse get(String email, UUID id) {
        User user = requireUser(email);
        return bookMapper.toDto(requireOwned(id, user));
    }

    @Transactional
    public BookResponse update(String email, UUID id, UpdateBookRequest req) {
        User user = requireUser(email);
        Book book = requireOwned(id, user);

        book.setTitle(req.title());
        book.setCoverImageUrl(req.coverImageUrl());
        book.setSourceUrl(req.sourceUrl());
        book.setStatus(req.status());
        book.setStartDate(req.startDate());
        book.setEndDate(req.endDate());

        return bookMapper.toDto(bookRepository.save(book));
    }

    @Transactional
    public BookResponse updateStatus(String email, UUID id, UpdateBookStatusRequest req) {
        User user = requireUser(email);
        Book book = requireOwned(id, user);

        if (req.startDate() != null) {
            book.setStartDate(req.startDate());
        } else if (req.status() == BookStatus.IN_PROGRESS && book.getStartDate() == null) {
            book.setStartDate(LocalDate.now());
        }

        if (req.endDate() != null) {
            book.setEndDate(req.endDate());
        } else if (req.status() == BookStatus.READ && book.getEndDate() == null) {
            book.setEndDate(LocalDate.now());
        }

        if (req.status() == BookStatus.READ && book.getStartDate() == null) {
            book.setStartDate(book.getEndDate());
        }

        book.setStatus(req.status());
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Transactional
    public void delete(String email, UUID id) {
        User user = requireUser(email);
        Book book = requireOwned(id, user);
        bookRepository.delete(book);
    }

    @Transactional
    public BookResponse refreshMetadata(String email, UUID id) {
        User user = requireUser(email);
        Book book = requireOwned(id, user);

        if (!StringUtils.hasText(book.getSourceUrl())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This book has no source link to refresh from");
        }

        BookMetadataResponse metadata = metadataScraper.scrape(book.getSourceUrl());
        if (StringUtils.hasText(metadata.title())) book.setTitle(metadata.title());
        if (StringUtils.hasText(metadata.coverImageUrl())) book.setCoverImageUrl(metadata.coverImageUrl());

        return bookMapper.toDto(bookRepository.save(book));
    }

    public BookMetadataResponse fetchMetadataPreview(String url) {
        return metadataScraper.scrape(url);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Book requireOwned(UUID id, User user) {
        return bookRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Book not found"));
    }
}
