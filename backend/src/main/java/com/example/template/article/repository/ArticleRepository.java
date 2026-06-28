package com.example.template.article.repository;

import com.example.template.article.entity.Article;
import com.example.template.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {

    List<Article> findAllByUser(User user);

    Optional<Article> findByIdAndUser(UUID id, User user);
}
