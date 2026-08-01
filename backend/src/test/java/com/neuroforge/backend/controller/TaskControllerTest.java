package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.TaskResponse;
import com.neuroforge.backend.dto.TaskStatusHistoryResponse;
import com.neuroforge.backend.dto.UpdateTaskStatusRequest;
import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private UUID taskId;
    private UUID sprintId;
    private Long userId;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        sprintId = UUID.randomUUID();
        userId = 1L;

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .title("Feature Implementation")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();
    }

    @Test
    void getBacklogTasks_ReturnsOk() {
        when(taskService.getBacklogTasks()).thenReturn(Collections.singletonList(taskResponse));

        ResponseEntity<List<TaskResponse>> result = taskController.getBacklogTasks();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void assignSprint_ReturnsOk() {
        taskResponse.setSprintId(sprintId);
        when(taskService.assignSprint(taskId, sprintId)).thenReturn(taskResponse);

        ResponseEntity<TaskResponse> result = taskController.assignSprint(taskId, sprintId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(sprintId, result.getBody().getSprintId());
    }

    @Test
    void removeSprint_ReturnsOk() {
        when(taskService.removeSprint(taskId)).thenReturn(taskResponse);

        ResponseEntity<TaskResponse> result = taskController.removeSprint(taskId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void assignUser_ReturnsOk() {
        taskResponse.setAssigneeId(userId);
        when(taskService.assignUser(taskId, userId)).thenReturn(taskResponse);

        ResponseEntity<TaskResponse> result = taskController.assignUser(taskId, userId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(userId, result.getBody().getAssigneeId());
    }

    @Test
    void removeUser_ReturnsOk() {
        when(taskService.removeUser(taskId)).thenReturn(taskResponse);

        ResponseEntity<TaskResponse> result = taskController.removeUser(taskId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void updateTaskStatus_ReturnsOk() {
        taskResponse.setStatus(TaskStatus.IN_PROGRESS);
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);
        when(taskService.updateTaskStatus(taskId, request)).thenReturn(taskResponse);

        ResponseEntity<TaskResponse> result = taskController.updateTaskStatus(taskId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(TaskStatus.IN_PROGRESS, result.getBody().getStatus());
    }

    @Test
    void searchTasks_ReturnsOk() {
        when(taskService.searchTasks("Feature")).thenReturn(Collections.singletonList(taskResponse));

        ResponseEntity<List<TaskResponse>> result = taskController.searchTasks("Feature");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getTaskStatusHistory_ReturnsOk() {
        TaskStatusHistoryResponse historyItem = TaskStatusHistoryResponse.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .previousStatus(TaskStatus.TODO)
                .newStatus(TaskStatus.IN_PROGRESS)
                .changedBy("SYSTEM")
                .changedAt(LocalDateTime.now())
                .build();

        when(taskService.getTaskStatusHistory(taskId)).thenReturn(Collections.singletonList(historyItem));

        ResponseEntity<List<TaskStatusHistoryResponse>> result = taskController.getTaskStatusHistory(taskId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(TaskStatus.TODO, result.getBody().get(0).getPreviousStatus());
        assertEquals(TaskStatus.IN_PROGRESS, result.getBody().get(0).getNewStatus());
    }
}
