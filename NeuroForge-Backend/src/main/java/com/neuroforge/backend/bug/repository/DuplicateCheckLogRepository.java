package com.neuroforge.backend.bug.repository;

import com.neuroforge.backend.bug.entity.DuplicateCheckLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DuplicateCheckLogRepository
        extends MongoRepository<DuplicateCheckLog, String> {

}