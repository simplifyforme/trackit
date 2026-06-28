package com.example.template.article.mapper;

import com.example.template.article.dto.ArticleResponse;
import com.example.template.article.entity.Article;
import org.mapstruct.Mapper;

@Mapper
public interface ArticleMapper {
    ArticleResponse toDto(Article article);
}
