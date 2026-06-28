package com.example.template.book.controller;

import com.example.template.book.dto.*;
import com.example.template.book.service.BookService;
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
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<BookResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
        return bookService.list(userDetails.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BookResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateBookRequest request) {
        return bookService.create(userDetails.getUsername(), request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BookResponse get(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return bookService.get(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BookResponse update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBookRequest request) {
        return bookService.update(userDetails.getUsername(), id, request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BookResponse updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBookStatusRequest request) {
        return bookService.updateStatus(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        bookService.delete(userDetails.getUsername(), id);
    }

    @PostMapping("/{id}/refresh-metadata")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BookResponse refreshMetadata(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return bookService.refreshMetadata(userDetails.getUsername(), id);
    }

    @PostMapping("/metadata-preview")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BookMetadataResponse metadataPreview(@Valid @RequestBody FetchBookMetadataRequest request) {
        return bookService.fetchMetadataPreview(request.url());
    }
}
