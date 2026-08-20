package com.neuroforge.backend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDistributionResponse {

    private Long todoTasks;
    private Long inProgressTasks;
    private Long codeReviewTasks;
    private Long testingTasks;
    private Long completedTasks;
}
