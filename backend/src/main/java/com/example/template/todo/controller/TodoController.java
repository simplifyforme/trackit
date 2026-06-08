package com.example.template.todo.controller;

import com.example.template.todo.dto.CreateTodoRequest;
import com.example.template.todo.dto.TodoResponse;
import com.example.template.todo.dto.UpdateTodoRequest;
import com.example.template.todo.service.TodoService;
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
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<TodoResponse> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Boolean showDone) {
        return todoService.list(userDetails.getUsername(), sortBy, showDone);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TodoResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateTodoRequest request) {
        return todoService.create(userDetails.getUsername(), request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TodoResponse get(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return todoService.get(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TodoResponse update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTodoRequest request) {
        return todoService.update(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        todoService.delete(userDetails.getUsername(), id);
    }
}
