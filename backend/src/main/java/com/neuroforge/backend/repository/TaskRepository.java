package com.neuroforge.backend.repository;

import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findBySprintIsNull();
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByPriority(TaskPriority priority);
    List<Task> findBySprintId(UUID sprintId);
    List<Task> findByAssigneeId(UUID userId);
    List<Task> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String titleKeyword, String descKeyword);
    long countBySprintId(UUID sprintId);
    long countBySprintIdAndStatus(UUID sprintId, TaskStatus status);
    long countBySprintIdAndPriority(UUID sprintId, TaskPriority priority);
    long countBySprintIdAndAssigneeId(UUID sprintId, UUID assigneeId);
    long countBySprintIdAndAssigneeIsNull(UUID sprintId);
}
