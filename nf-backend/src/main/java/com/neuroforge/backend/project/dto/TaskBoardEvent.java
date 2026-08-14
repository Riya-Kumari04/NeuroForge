package com.neuroforge.backend.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Module 5: DTO for Task Board Events
 * 
 * Represents a task status change event that is broadcast via WebSocket
 * to all users viewing the same project's kanban board.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskBoardEvent {
    
    private Long taskId;
    private Long projectId;
    private String previousStatus;
    private String newStatus;
    private String changedBy;
    private LocalDateTime timestamp;
}
