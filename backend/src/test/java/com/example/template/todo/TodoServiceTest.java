package com.example.template.todo;

import com.example.template.exception.ApiException;
import com.example.template.todo.dto.CreateTodoRequest;
import com.example.template.todo.dto.TodoResponse;
import com.example.template.todo.entity.ImportanceLevel;
import com.example.template.todo.entity.Todo;
import com.example.template.todo.mapper.TodoMapper;
import com.example.template.todo.repository.TodoRepository;
import com.example.template.todo.service.TodoService;
import com.example.template.user.entity.User;
import com.example.template.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock TodoRepository todoRepository;
    @Mock UserRepository userRepository;
    @Mock TodoMapper todoMapper;

    @InjectMocks TodoService todoService;

    User user;
    Todo todo;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        todo = Todo.builder()
                .id(UUID.randomUUID())
                .user(user)
                .title("Buy milk")
                .importance(ImportanceLevel.HIGH)
                .isDone(false)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void list_returnsTodosForUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(todoRepository.findAllByUser(user)).thenReturn(List.of(todo));
        TodoResponse resp = new TodoResponse(todo.getId(), todo.getTitle(), null,
                ImportanceLevel.HIGH, null, false, todo.getCreatedAt(), null);
        when(todoMapper.toDto(todo)).thenReturn(resp);

        List<TodoResponse> result = todoService.list("test@example.com", null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Buy milk");
    }

    @Test
    void list_filtersByIsDone() {
        Todo done = Todo.builder().id(UUID.randomUUID()).user(user).title("Done task")
                .isDone(true).importance(ImportanceLevel.LOW).createdAt(Instant.now()).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(todoRepository.findAllByUser(user)).thenReturn(List.of(todo, done));

        TodoResponse doneResp = new TodoResponse(done.getId(), done.getTitle(), null,
                ImportanceLevel.LOW, null, true, done.getCreatedAt(), null);
        when(todoMapper.toDto(done)).thenReturn(doneResp);

        List<TodoResponse> result = todoService.list("test@example.com", null, true);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isDone()).isTrue();
    }

    @Test
    void list_sortsByImportanceDescending() {
        Todo low = Todo.builder().id(UUID.randomUUID()).user(user).title("Low")
                .importance(ImportanceLevel.LOW).createdAt(Instant.now()).build();
        Todo critical = Todo.builder().id(UUID.randomUUID()).user(user).title("Critical")
                .importance(ImportanceLevel.CRITICAL).createdAt(Instant.now()).build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(todoRepository.findAllByUser(user)).thenReturn(List.of(low, critical));

        TodoResponse lowResp = new TodoResponse(low.getId(), "Low", null, ImportanceLevel.LOW, null, false, Instant.now(), null);
        TodoResponse critResp = new TodoResponse(critical.getId(), "Critical", null, ImportanceLevel.CRITICAL, null, false, Instant.now(), null);
        when(todoMapper.toDto(low)).thenReturn(lowResp);
        when(todoMapper.toDto(critical)).thenReturn(critResp);

        List<TodoResponse> result = todoService.list("test@example.com", "importance", null);
        assertThat(result.get(0).importance()).isEqualTo(ImportanceLevel.CRITICAL);
        assertThat(result.get(1).importance()).isEqualTo(ImportanceLevel.LOW);
    }

    @Test
    void list_sortsByDeadlineNullsLast() {
        Instant soon = Instant.now().plusSeconds(3600);
        Todo withDeadline = Todo.builder().id(UUID.randomUUID()).user(user).title("Soon")
                .importance(ImportanceLevel.MEDIUM).deadline(soon).createdAt(Instant.now()).build();
        Todo noDeadline = Todo.builder().id(UUID.randomUUID()).user(user).title("No deadline")
                .importance(ImportanceLevel.MEDIUM).createdAt(Instant.now()).build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(todoRepository.findAllByUser(user)).thenReturn(List.of(noDeadline, withDeadline));

        TodoResponse wdResp = new TodoResponse(withDeadline.getId(), "Soon", null, ImportanceLevel.MEDIUM, soon, false, Instant.now(), null);
        TodoResponse ndResp = new TodoResponse(noDeadline.getId(), "No deadline", null, ImportanceLevel.MEDIUM, null, false, Instant.now(), null);
        when(todoMapper.toDto(withDeadline)).thenReturn(wdResp);
        when(todoMapper.toDto(noDeadline)).thenReturn(ndResp);

        List<TodoResponse> result = todoService.list("test@example.com", "deadline", null);
        assertThat(result.get(0).deadline()).isNotNull();
        assertThat(result.get(1).deadline()).isNull();
    }

    @Test
    void create_savesAndReturnsTodo() {
        CreateTodoRequest req = new CreateTodoRequest("New task", null, ImportanceLevel.HIGH, null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);
        TodoResponse resp = new TodoResponse(todo.getId(), "New task", null, ImportanceLevel.HIGH, null, false, Instant.now(), null);
        when(todoMapper.toDto(todo)).thenReturn(resp);

        TodoResponse result = todoService.create("test@example.com", req);
        assertThat(result.title()).isEqualTo("New task");
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void create_setsDefaultImportanceMedium() {
        CreateTodoRequest req = new CreateTodoRequest("Task", null, null, null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(todoRepository.save(any(Todo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(todoMapper.toDto(any())).thenReturn(new TodoResponse(UUID.randomUUID(), "Task", null, ImportanceLevel.MEDIUM, null, false, Instant.now(), null));

        todoService.create("test@example.com", req);

        verify(todoRepository).save(argThat(t -> t.getImportance() == ImportanceLevel.MEDIUM));
    }

    @Test
    void get_throwsNotFoundForWrongUser() {
        User other = User.builder().id(UUID.randomUUID()).email("other@example.com").build();
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(other));
        when(todoRepository.findByIdAndUser(todo.getId(), other)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.get("other@example.com", todo.getId()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void delete_removesTodo() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(todoRepository.findByIdAndUser(todo.getId(), user)).thenReturn(Optional.of(todo));

        todoService.delete("test@example.com", todo.getId());

        verify(todoRepository).delete(todo);
    }

    @Test
    void createdAt_isNotNull_afterCreate() {
        // Verify the entity carries a createdAt (set by @CreationTimestamp on save).
        // In a real integration test Hibernate sets it; here we confirm the field is present in the DTO.
        CreateTodoRequest req = new CreateTodoRequest("With timestamp", null, null, null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);
        Instant now = Instant.now();
        when(todoMapper.toDto(todo)).thenReturn(new TodoResponse(todo.getId(), "With timestamp", null, ImportanceLevel.MEDIUM, null, false, now, now));

        TodoResponse result = todoService.create("test@example.com", req);
        assertThat(result.createdAt()).isNotNull();
    }
}
