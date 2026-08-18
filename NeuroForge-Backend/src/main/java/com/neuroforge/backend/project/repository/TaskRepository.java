package com.neuroforge.backend.project.repository;

import com.neuroforge.backend.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findBySprintId(Long sprintId);

    List<Task> findByAssignedToId(Long projectMemberId);

    List<Task> findByProjectIdAndSprintId(Long projectId, Long sprintId);

    // Dashboard
    long countByStatus(String status);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, String status);

    // Task Board
    List<Task> findByProjectIdAndStatus(Long projectId, String status);
}
