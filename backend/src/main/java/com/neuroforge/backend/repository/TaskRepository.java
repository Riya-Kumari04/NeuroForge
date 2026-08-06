package com.neuroforge.backend.repository;

import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findBySprintIsNull();
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByPriority(TaskPriority priority);
    List<Task> findBySprintId(UUID sprintId);
    List<Task> findByAssigneeId(Long userId);
    List<Task> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String titleKeyword, String descKeyword);
    long countBySprintId(UUID sprintId);
    long countBySprintIdAndStatus(UUID sprintId, TaskStatus status);
    long countBySprintIdAndPriority(UUID sprintId, TaskPriority priority);
    long countBySprintIdAndAssigneeId(UUID sprintId, Long assigneeId);
    long countBySprintIdAndAssigneeIsNull(UUID sprintId);

    long countByStatus(TaskStatus status);

    @Query("""
    SELECT COALESCE(SUM(t.storyPoints),0)
    FROM Task t
    """)
    Integer getTotalStoryPoints();

    @Query("""
    SELECT COALESCE(SUM(t.storyPoints),0)
    FROM Task t
    WHERE t.status = :status
    """)
    Integer getStoryPointsByStatus(@Param("status") TaskStatus status);

    @Query("""
    SELECT COALESCE(SUM(t.storyPoints),0)
    FROM Task t
    WHERE t.sprint.id = :sprintId
    """)
    Integer getTotalStoryPointsBySprint(@Param("sprintId") UUID sprintId);

    @Query("""
    SELECT COALESCE(SUM(t.storyPoints),0)
    FROM Task t
    WHERE t.sprint.id = :sprintId
    AND t.status = :status
    """)
    Integer getStoryPointsBySprintAndStatus(@Param("sprintId") UUID sprintId, @Param("status") TaskStatus status);

    long countByAssigneeId(Long userId);

    long countByAssigneeIdAndStatus(Long userId, TaskStatus status);

    @Query("""
    SELECT COALESCE(SUM(t.storyPoints),0)
    FROM Task t
    WHERE t.assignee.id = :userId
    """)
    Integer getTotalStoryPointsByAssignee(@Param("userId") Long userId);

    @Query("""
    SELECT COALESCE(SUM(t.storyPoints),0)
    FROM Task t
    WHERE t.assignee.id = :userId
    AND t.status = :status
    """)
    Integer getStoryPointsByAssigneeAndStatus(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status);
}
