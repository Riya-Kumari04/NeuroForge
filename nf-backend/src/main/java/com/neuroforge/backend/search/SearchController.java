package com.neuroforge.backend.search;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.dto.UserDTO;
import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.organization.dto.OrganizationDto;
import com.neuroforge.backend.organization.dto.TeamDto;
import com.neuroforge.backend.organization.repository.OrganizationRepository;
import com.neuroforge.backend.organization.repository.TeamRepository;
import com.neuroforge.backend.project.entity.Project;
import com.neuroforge.backend.project.entity.Sprint;
import com.neuroforge.backend.project.entity.Task;
import com.neuroforge.backend.project.repository.ProjectRepository;
import com.neuroforge.backend.project.repository.SprintRepository;
import com.neuroforge.backend.project.repository.TaskRepository;
import com.neuroforge.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Global Search")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private final UserRepository         userRepo;
    private final OrganizationRepository orgRepo;
    private final TeamRepository         teamRepo;
    private final ProjectRepository      projectRepo;
    private final SprintRepository       sprintRepo;
    private final TaskRepository         taskRepo;

    @GetMapping
    @Operation(summary = "Global search across all entities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @RequestParam String q,
            @AuthenticationPrincipal User currentUser) {

        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok("Empty query", Map.of()));
        }

        String term = q.toLowerCase().trim();
        boolean isSuperAdmin = "ROLE_SUPER_ADMIN".equals(currentUser.getRole());
        boolean isOrgAdmin   = "ROLE_ORG_ADMIN".equals(currentUser.getRole());

        Map<String, Object> results = new LinkedHashMap<>();

        // Users — super admin / org admin only
        if (isSuperAdmin || isOrgAdmin) {
            List<UserDTO> users = userRepo.findAll().stream()
                    .filter(u -> match(u.getName(), term) || match(u.getEmail(), term) || match(u.getUsername(), term))
                    .map(UserDTO::from)
                    .limit(10)
                    .collect(Collectors.toList());
            if (!users.isEmpty()) results.put("users", users);
        }

        // Organizations — super admin / org admin only
        if (isSuperAdmin || isOrgAdmin) {
            List<OrganizationDto> orgs = orgRepo.findAll().stream()
                    .filter(o -> match(o.getName(), term) || match(o.getSlug(), term))
                    .map(OrganizationDto::from)
                    .limit(10)
                    .collect(Collectors.toList());
            if (!orgs.isEmpty()) results.put("organizations", orgs);
        }

        // Teams
        List<TeamDto> teams = teamRepo.findAll().stream()
                .filter(t -> match(t.getName(), term) || match(t.getDescription(), term))
                .map(TeamDto::from)
                .limit(10)
                .collect(Collectors.toList());
        if (!teams.isEmpty()) results.put("teams", teams);

        // Projects
        List<Map<String, Object>> projects = projectRepo.findAll().stream()
                .filter(p -> match(p.getProjectName(), term) || match(p.getDescription(), term))
                .map(p -> projectMap(p))
                .limit(10)
                .collect(Collectors.toList());
        if (!projects.isEmpty()) results.put("projects", projects);

        // Sprints
        List<Map<String, Object>> sprints = sprintRepo.findAll().stream()
                .filter(s -> match(s.getSprintName(), term) || match(s.getGoal(), term))
                .map(s -> sprintMap(s))
                .limit(10)
                .collect(Collectors.toList());
        if (!sprints.isEmpty()) results.put("sprints", sprints);

        // Tasks
        List<Map<String, Object>> tasks = taskRepo.findAll().stream()
                .filter(t -> match(t.getTitle(), term) || match(t.getDescription(), term))
                .map(t -> taskMap(t))
                .limit(10)
                .collect(Collectors.toList());
        if (!tasks.isEmpty()) results.put("tasks", tasks);

        return ResponseEntity.ok(ApiResponse.ok("Search results", results));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get search suggestions/autocomplete")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> suggestions(
            @RequestParam String q,
            @AuthenticationPrincipal User currentUser) {

        if (q == null || q.isBlank() || q.length() < 2) {
            return ResponseEntity.ok(ApiResponse.ok("Empty query", List.of()));
        }

        String term = q.toLowerCase().trim();
        List<Map<String, Object>> suggestions = new ArrayList<>();

        // Add project suggestions
        projectRepo.findAll().stream()
                .filter(p -> match(p.getProjectName(), term))
                .limit(5)
                .forEach(p -> {
                    Map<String, Object> suggestion = new LinkedHashMap<>();
                    suggestion.put("type", "project");
                    suggestion.put("id", p.getId());
                    suggestion.put("title", p.getProjectName());
                    suggestion.put("subtitle", p.getOrganization() != null ? p.getOrganization().getName() : "");
                    suggestions.add(suggestion);
                });

        // Add task suggestions
        taskRepo.findAll().stream()
                .filter(t -> match(t.getTitle(), term))
                .limit(5)
                .forEach(t -> {
                    Map<String, Object> suggestion = new LinkedHashMap<>();
                    suggestion.put("type", "task");
                    suggestion.put("id", t.getId());
                    suggestion.put("title", t.getTitle());
                    suggestion.put("subtitle", t.getStatus());
                    suggestions.add(suggestion);
                });

        // Add sprint suggestions
        sprintRepo.findAll().stream()
                .filter(s -> match(s.getSprintName(), term))
                .limit(3)
                .forEach(s -> {
                    Map<String, Object> suggestion = new LinkedHashMap<>();
                    suggestion.put("type", "sprint");
                    suggestion.put("id", s.getId());
                    suggestion.put("title", s.getSprintName());
                    suggestion.put("subtitle", s.getStatus());
                    suggestions.add(suggestion);
                });

        return ResponseEntity.ok(ApiResponse.ok("Suggestions", suggestions));
    }

    private boolean match(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }

    private Map<String, Object> projectMap(Project p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("projectName", p.getProjectName());
        m.put("description", p.getDescription());
        m.put("status", p.getStatus());
        m.put("organizationName", p.getOrganization() != null ? p.getOrganization().getName() : null);
        return m;
    }

    private Map<String, Object> sprintMap(Sprint s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("name", s.getSprintName());
        m.put("goal", s.getGoal());
        m.put("status", s.getStatus());
        return m;
    }

    private Map<String, Object> taskMap(Task t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("title", t.getTitle());
        m.put("description", t.getDescription());
        m.put("status", t.getStatus());
        m.put("priority", t.getPriority());
        return m;
    }
}
