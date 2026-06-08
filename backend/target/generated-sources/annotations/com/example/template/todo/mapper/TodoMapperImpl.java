package com.example.template.todo.mapper;

import com.example.template.todo.dto.TodoResponse;
import com.example.template.todo.entity.ImportanceLevel;
import com.example.template.todo.entity.Todo;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T17:33:59+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class TodoMapperImpl implements TodoMapper {

    @Override
    public TodoResponse toDto(Todo todo) {
        if ( todo == null ) {
            return null;
        }

        UUID id = null;
        String title = null;
        String description = null;
        ImportanceLevel importance = null;
        Instant deadline = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = todo.getId();
        title = todo.getTitle();
        description = todo.getDescription();
        importance = todo.getImportance();
        deadline = todo.getDeadline();
        createdAt = todo.getCreatedAt();
        updatedAt = todo.getUpdatedAt();

        boolean isDone = false;

        TodoResponse todoResponse = new TodoResponse( id, title, description, importance, deadline, isDone, createdAt, updatedAt );

        return todoResponse;
    }
}
