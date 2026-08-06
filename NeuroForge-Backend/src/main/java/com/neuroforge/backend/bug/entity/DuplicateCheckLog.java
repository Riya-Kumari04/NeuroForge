package com.neuroforge.backend.bug.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "duplicate_check_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuplicateCheckLog {

    @Id
    private String id;

    private Long existingBugId;

    private String existingTitle;

    private String newTitle;

    private boolean duplicate;

    private LocalDateTime checkedAt;
}