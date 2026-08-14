package com.neuroforge.backend.integration.scheduler;

import com.neuroforge.backend.integration.entity.RepositoryConnection;
import com.neuroforge.backend.integration.repository.RepositoryConnectionRepository;
import com.neuroforge.backend.integration.service.RepositoryConnectionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepositorySyncScheduler {

    private final RepositoryConnectionRepository repositoryConnectionRepository;
    private final RepositoryConnectionService repositoryConnectionService;

    @PostConstruct
    public void init() {
        log.info("RepositorySyncScheduler Loaded");
    }

    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void syncRepositories() {

        log.info("===== Repository Sync Scheduler Running =====");

        repositoryConnectionRepository.findAll()
                .stream()
                .filter(RepositoryConnection::getConnected)
                .forEach(repository -> {
                    try {
                        log.info("Syncing repository: {}", repository.getRepositoryName());
                        repositoryConnectionService.syncRepository(repository.getId());
                        log.info("Sync completed for: {}", repository.getRepositoryName());
                    } catch (Exception e) {
                        log.error("Error syncing repository: {}", repository.getRepositoryName(), e);
                    }
                });
    }
}
