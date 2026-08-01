package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.TaskResponse;
import com.neuroforge.backend.dto.TaskStatusHistoryResponse;
import com.neuroforge.backend.dto.UpdateTaskStatusRequest;
import com.neuroforge.backend.entity.CodeReview;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.entity.TaskStatusHistory;
import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.enums.CodeReviewStatus;
import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.InvalidTaskStateException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.CodeReviewRepository;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TaskStatusHistoryRepository;
import com.neuroforge.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskStatusHistoryRepository taskStatusHistoryRepository;

    @Mock
    private CodeReviewRepository codeReviewRepository;

    @InjectMocks
    private TaskService taskService;

    private UUID taskId;
    private UUID sprintId;
    private Long userId;
    private Task task;
    private Sprint sprint;
    private User user;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        sprintId = UUID.randomUUID();
        userId = 1L;

        sprint = Sprint.builder().id(sprintId).name("Sprint 1").build();
        user = new User(userId, "developer@neuroforge.com");

        task = Task.builder()
                .id(taskId)
                .title("Build Authentication")
                .description("Implement JWT auth")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .storyPoints(5)
                .build();
    }

    @Test
    void getBacklogTasks_ReturnsTasksWithNullSprint() {
        when(taskRepository.findBySprintIsNull()).thenReturn(Collections.singletonList(task));

        List<TaskResponse> backlog = taskService.getBacklogTasks();

        assertNotNull(backlog);
        assertEquals(1, backlog.size());
        assertNull(backlog.get(0).getSprintId());
    }

    @Test
    void assignSprint_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.assignSprint(taskId, sprintId);

        assertNotNull(response);
        assertEquals(sprintId, response.getSprintId());
    }

    @Test
    void removeSprint_Success() {
        task.setSprint(sprint);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.removeSprint(taskId);

        assertNotNull(response);
        assertNull(response.getSprintId());
    }

    @Test
    void assignUser_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.assignUser(taskId, userId);

        assertNotNull(response);
        assertEquals(userId, response.getAssigneeId());
    }

    @Test
    void removeUser_Success() {
        task.setAssignee(user);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.removeUser(taskId);

        assertNotNull(response);
        assertNull(response.getAssigneeId());
    }

    @Test
    void updateTaskStatus_ValidTransitions_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        // TODO -> IN_PROGRESS
        TaskResponse response = taskService.updateTaskStatus(taskId, new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS));
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());

        // IN_PROGRESS -> CODE_REVIEW
        task.setStatus(TaskStatus.IN_PROGRESS);
        response = taskService.updateTaskStatus(taskId, new UpdateTaskStatusRequest(TaskStatus.CODE_REVIEW));
        assertEquals(TaskStatus.CODE_REVIEW, response.getStatus());

        // CODE_REVIEW -> TESTING
        task.setStatus(TaskStatus.CODE_REVIEW);
        when(codeReviewRepository.findTopByTaskIdOrderByCreatedAtDesc(taskId))
                .thenReturn(Optional.of(CodeReview.builder().status(CodeReviewStatus.ACCEPTED).build()));
        response = taskService.updateTaskStatus(taskId, new UpdateTaskStatusRequest(TaskStatus.TESTING));
        assertEquals(TaskStatus.TESTING, response.getStatus());

        // TESTING -> DONE
        task.setStatus(TaskStatus.TESTING);
        response = taskService.updateTaskStatus(taskId, new UpdateTaskStatusRequest(TaskStatus.DONE));
        assertEquals(TaskStatus.DONE, response.getStatus());
    }

    @Test
    void updateTaskStatus_InvalidTransition_ThrowsException() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // TODO -> DONE (Invalid)
        assertThrows(InvalidTaskStateException.class, () ->
                taskService.updateTaskStatus(taskId, new UpdateTaskStatusRequest(TaskStatus.DONE))
        );

        // TODO -> CODE_REVIEW (Invalid)
        assertThrows(InvalidTaskStateException.class, () ->
                taskService.updateTaskStatus(taskId, new UpdateTaskStatusRequest(TaskStatus.CODE_REVIEW))
        );
    }

    @Test
    void filterAndSearch_Success() {
        when(taskRepository.findByStatus(TaskStatus.TODO)).thenReturn(Collections.singletonList(task));
        when(taskRepository.findByPriority(TaskPriority.HIGH)).thenReturn(Collections.singletonList(task));
        when(taskRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("auth", "auth"))
                .thenReturn(Collections.singletonList(task));

        assertEquals(1, taskService.getTasksByStatus(TaskStatus.TODO).size());
        assertEquals(1, taskService.getTasksByPriority(TaskPriority.HIGH).size());
        assertEquals(1, taskService.searchTasks("auth").size());
    }

    @Test
    void getTaskStatusHistory_Success() {
        when(taskRepository.existsById(taskId)).thenReturn(true);

        TaskStatusHistory historyItem = TaskStatusHistory.builder()
                .id(UUID.randomUUID())
                .task(task)
                .previousStatus(TaskStatus.TODO)
                .newStatus(TaskStatus.IN_PROGRESS)
                .changedBy("SYSTEM")
                .changedAt(LocalDateTime.now())
                .build();

        when(taskStatusHistoryRepository.findByTaskIdOrderByChangedAtAsc(taskId))
                .thenReturn(Collections.singletonList(historyItem));

        List<TaskStatusHistoryResponse> history = taskService.getTaskStatusHistory(taskId);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(TaskStatus.TODO, history.get(0).getPreviousStatus());
        assertEquals(TaskStatus.IN_PROGRESS, history.get(0).getNewStatus());
        assertEquals("SYSTEM", history.get(0).getChangedBy());
    }
}
