package com.example.template.todo.service;

import com.example.template.exception.ApiException;
import com.example.template.todo.dto.CreateTodoRequest;
import com.example.template.todo.dto.TodoResponse;
import com.example.template.todo.dto.UpdateTodoRequest;
import com.example.template.todo.entity.ImportanceLevel;
import com.example.template.todo.entity.Todo;
import com.example.template.todo.mapper.TodoMapper;
import com.example.template.todo.repository.TodoRepository;
import com.example.template.user.entity.User;
import com.example.template.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoMapper todoMapper;

    @Transactional(readOnly = true)
    public List<TodoResponse> list(String email, String sortBy, Boolean showDone) {
        User user = requireUser(email);
        List<Todo> todos = todoRepository.findAllByUser(user);

        if (showDone != null) {
            todos = todos.stream().filter(t -> t.isDone() == showDone).toList();
        }

        Comparator<Todo> comparator = switch (sortBy != null ? sortBy : "createdAt") {
            case "importance" -> Comparator.comparingInt(
                    (Todo t) -> t.getImportance().weight()).reversed();
            case "deadline"   -> Comparator.comparing(
                    Todo::getDeadline,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default           -> Comparator.comparing(Todo::getCreatedAt).reversed();
        };

        return todos.stream().sorted(comparator).map(todoMapper::toDto).toList();
    }

    @Transactional
    public TodoResponse create(String email, CreateTodoRequest req) {
        User user = requireUser(email);
        Todo todo = Todo.builder()
                .user(user)
                .title(req.title())
                .description(req.description())
                .importance(req.importance() != null ? req.importance() : ImportanceLevel.MEDIUM)
                .deadline(req.deadline())
                .build();
        return todoMapper.toDto(todoRepository.save(todo));
    }

    @Transactional(readOnly = true)
    public TodoResponse get(String email, UUID id) {
        User user = requireUser(email);
        return todoMapper.toDto(requireOwned(id, user));
    }

    @Transactional
    public TodoResponse update(String email, UUID id, UpdateTodoRequest req) {
        User user = requireUser(email);
        Todo todo = requireOwned(id, user);

        todo.setTitle(req.title());
        todo.setDescription(req.description());
        if (req.importance() != null) todo.setImportance(req.importance());
        todo.setDeadline(req.deadline());
        if (req.isDone() != null) todo.setDone(req.isDone());

        return todoMapper.toDto(todoRepository.save(todo));
    }

    @Transactional
    public void delete(String email, UUID id) {
        User user = requireUser(email);
        Todo todo = requireOwned(id, user);
        todoRepository.delete(todo);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Todo requireOwned(UUID id, User user) {
        return todoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Todo not found"));
    }
}
