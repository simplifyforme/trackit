package com.example.template.todo.mapper;

import com.example.template.todo.dto.TodoResponse;
import com.example.template.todo.entity.Todo;
import org.mapstruct.Mapper;

@Mapper
public interface TodoMapper {
    TodoResponse toDto(Todo todo);
}
