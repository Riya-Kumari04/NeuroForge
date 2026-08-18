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
        System.out.println("RepositorySyncScheduler Loaded");
    }

    @Scheduled(fixedRate = 10000) // every 10 seconds for testing
    public void syncRepositories() {

        System.out.println("===== Scheduler Running =====");

        repositoryConnectionRepository.findAll()
                .stream()
                .filter(RepositoryConnection::getConnected)
                .forEach(repository -> {
                    try {
                        System.out.println("Syncing " + repository.getRepositoryName());
                        repositoryConnectionService.syncRepository(repository.getId());
                        System.out.println("Sync completed");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}