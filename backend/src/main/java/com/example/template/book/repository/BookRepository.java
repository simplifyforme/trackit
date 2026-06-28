package com.example.template.book.repository;

import com.example.template.book.entity.Book;
import com.example.template.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    List<Book> findAllByUser(User user);

    Optional<Book> findByIdAndUser(UUID id, User user);
}
