package com.example.template.book.mapper;

import com.example.template.book.dto.BookResponse;
import com.example.template.book.entity.Book;
import org.mapstruct.Mapper;

@Mapper
public interface BookMapper {
    BookResponse toDto(Book book);
}
