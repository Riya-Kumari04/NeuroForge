package com.neuroforge.backend.project.service;

import com.neuroforge.backend.project.entity.Sprint;
import com.neuroforge.backend.project.entity.StoryPointSnapshot;
import com.neuroforge.backend.project.entity.Task;
import com.neuroforge.backend.project.repository.SprintRepository;
import com.neuroforge.backend.project.repository.StoryPointSnapshotRepository;
import com.neuroforge.backend.project.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Module 5: Sprint Snapshot Scheduler
 * 
 * Scheduled task that creates daily story point snapshots for active sprints.
 * Runs at midnight every day to capture the remaining story points for accurate burndown charts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SprintSnapshotScheduler {

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final StoryPointSnapshotRepository snapshotRepository;

    /**
     * Scheduled task to create story point snapshots for all active sprints.
     * Runs at midnight (00:00) every day.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void createDailySnapshots() {
        log.info("Starting daily story point snapshot creation");
        
        // Find all active sprints
        List<Sprint> activeSprints = sprintRepository.findByStatus("ACTIVE");
        
        if (activeSprints.isEmpty()) {
            log.info("No active sprints found for snapshot creation");
            return;
        }
        
        LocalDate today = LocalDate.now();
        int snapshotsCreated = 0;
        
        for (Sprint sprint : activeSprints) {
            try {
                // Skip if snapshot already exists for today
                if (snapshotRepository.existsBySprintIdAndSnapshotDate(sprint.getId(), today)) {
                    log.debug("Snapshot already exists for sprint {} on {}", sprint.getId(), today);
                    continue;
                }
                
                // Calculate remaining story points for this sprint
                List<Task> sprintTasks = taskRepository.findBySprintId(sprint.getId());
                int remainingStoryPoints = sprintTasks.stream()
                        .filter(task -> !"DONE".equals(task.getStatus()))
                        .mapToInt(task -> task.getStoryPoints() != null ? task.getStoryPoints() : 0)
                        .sum();
                
                // Create and save snapshot
                StoryPointSnapshot snapshot = StoryPointSnapshot.builder()
                        .sprint(sprint)
                        .snapshotDate(today)
                        .remainingStoryPoints(remainingStoryPoints)
                        .build();
                
                snapshotRepository.save(snapshot);
                snapshotsCreated++;
                
                log.info("Created snapshot for sprint {}: {} remaining story points", 
                        sprint.getId(), remainingStoryPoints);
                        
            } catch (Exception e) {
                log.error("Failed to create snapshot for sprint {}", sprint.getId(), e);
            }
        }
        
        log.info("Daily snapshot creation completed. Created {} snapshots for {} active sprints", 
                snapshotsCreated, activeSprints.size());
    }

    /**
     * Manual trigger for creating snapshots (useful for testing or on-demand creation)
     */
    @Transactional
    public void createSnapshotForSprint(Long sprintId) {
        log.info("Creating manual snapshot for sprint {}", sprintId);
        
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new IllegalArgumentException("Sprint not found: " + sprintId));
        
        LocalDate today = LocalDate.now();
        
        // Skip if snapshot already exists for today
        if (snapshotRepository.existsBySprintIdAndSnapshotDate(sprintId, today)) {
            log.info("Snapshot already exists for sprint {} on {}", sprintId, today);
            return;
        }
        
        // Calculate remaining story points
        List<Task> sprintTasks = taskRepository.findBySprintId(sprintId);
        int remainingStoryPoints = sprintTasks.stream()
                .filter(task -> !"DONE".equals(task.getStatus()))
                .mapToInt(task -> task.getStoryPoints() != null ? task.getStoryPoints() : 0)
                .sum();
        
        // Create and save snapshot
        StoryPointSnapshot snapshot = StoryPointSnapshot.builder()
                .sprint(sprint)
                .snapshotDate(today)
                .remainingStoryPoints(remainingStoryPoints)
                .build();
        
        snapshotRepository.save(snapshot);
        
        log.info("Manual snapshot created for sprint {}: {} remaining story points", 
                sprintId, remainingStoryPoints);
    }
}
