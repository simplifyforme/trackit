package com.example.template.todo.repository;

import com.example.template.todo.entity.Todo;
import com.example.template.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, UUID> {

    List<Todo> findAllByUser(User user);

    Optional<Todo> findByIdAndUser(UUID id, User user);

    boolean existsByIdAndUser(UUID id, User user);
}
