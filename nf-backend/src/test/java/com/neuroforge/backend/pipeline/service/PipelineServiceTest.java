package com.neuroforge.backend.pipeline.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.pipeline.dto.*;
import com.neuroforge.backend.pipeline.entity.*;
import com.neuroforge.backend.pipeline.repository.*;
import com.neuroforge.backend.project.entity.Task;
import com.neuroforge.backend.project.repository.TaskRepository;
import com.neuroforge.backend.ai.service.GroqService;
import com.neuroforge.backend.ai.dto.ReleaseNotesRequest;
import com.neuroforge.backend.ai.dto.ReleaseNotesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @Mock
    private PipelineRunRepository pipelineRunRepository;

    @Mock
    private PipelineStageRepository pipelineStageRepository;

    @Mock
    private PipelineSimulator pipelineSimulator;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private ReleaseTaskRepository releaseTaskRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private GroqService groqService;

    @InjectMocks
    private PipelineServiceImpl pipelineService;

    private Pipeline pipeline;
    private PipelineRun pipelineRun;
    private Release release;
    private Task doneTask;
    private Task todoTask;

    @BeforeEach
    void setUp() {
        pipeline = Pipeline.builder()
                .id(1L)
                .name("Test Pipeline")
                .repositoryName("test-repo")
                .defaultBranch("main")
                .active(true)
                .build();

        pipelineRun = PipelineRun.builder()
                .id(1L)
                .pipeline(pipeline)
                .status("RUNNING")
                .startedAt(LocalDateTime.now())
                .triggeredBy("test@example.com")
                .build();

        release = Release.builder()
                .id(1L)
                .version("1.0.0")
                .status("DRAFT")
                .createdAt(LocalDateTime.now())
                .build();

        doneTask = Task.builder()
                .id(1L)
                .title("Completed Task")
                .status("DONE")
                .build();

        todoTask = Task.builder()
                .id(2L)
                .title("Incomplete Task")
                .status("TODO")
                .build();
    }

    @Test
    void testRunPipeline() {
        RunPipelineRequest request = new RunPipelineRequest();
        request.setPipelineId(1L);

        when(pipelineRepository.findById(1L)).thenReturn(Optional.of(pipeline));
        when(pipelineRunRepository.saveAndFlush(any(PipelineRun.class))).thenReturn(pipelineRun);

        ApiResponse<PipelineRunResponse> response = pipelineService.runPipeline(request);

        assertNotNull(response);
        assertEquals("Pipeline started successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getRunId());
        assertEquals("Test Pipeline", response.getData().getPipelineName());

        verify(pipelineSimulator, times(1)).simulate(1L);
    }

    @Test
    void testCreateRelease() {
        CreateReleaseRequest request = new CreateReleaseRequest();
        request.setVersion("1.0.0");
        request.setTaskIds(Arrays.asList(1L, 2L));

        when(releaseRepository.save(any(Release.class))).thenReturn(release);

        ApiResponse<ReleaseResponse> response = pipelineService.createRelease(request);

        assertNotNull(response);
        assertEquals("Release created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("1.0.0", response.getData().getVersion());
        assertEquals("DRAFT", response.getData().getStatus());

        verify(releaseTaskRepository, times(2)).save(any(ReleaseTask.class));
    }

    @Test
    void testGenerateReleaseNotes_WithDoneTasks() {
        ReleaseTask releaseTask1 = ReleaseTask.builder().id(1L).release(release).taskId(1L).build();
        ReleaseTask releaseTask2 = ReleaseTask.builder().id(2L).release(release).taskId(2L).build();

        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(releaseTaskRepository.findByReleaseId(1L)).thenReturn(Arrays.asList(releaseTask1, releaseTask2));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(doneTask));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(todoTask));
        when(groqService.generateReleaseNotes(any(ReleaseNotesRequest.class)))
                .thenReturn(ReleaseNotesResponse.builder().releaseNotes("AI Generated Notes").build());
        when(releaseRepository.save(any(Release.class))).thenReturn(release);

        ApiResponse<ReleaseNoteResponse> response = pipelineService.generateReleaseNotes(1L);

        assertNotNull(response);
        assertEquals("Release notes generated successfully", response.getMessage());
        assertNotNull(response.getData());

        verify(groqService, times(1)).generateReleaseNotes(any(ReleaseNotesRequest.class));
    }

    @Test
    void testGenerateReleaseNotes_OnlyDoneTasksFiltered() {
        ReleaseTask releaseTask1 = ReleaseTask.builder().id(1L).release(release).taskId(1L).build();
        ReleaseTask releaseTask2 = ReleaseTask.builder().id(2L).release(release).taskId(2L).build();

        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(releaseTaskRepository.findByReleaseId(1L)).thenReturn(Arrays.asList(releaseTask1, releaseTask2));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(doneTask));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(todoTask));
        when(groqService.generateReleaseNotes(any(ReleaseNotesRequest.class)))
                .thenReturn(ReleaseNotesResponse.builder().releaseNotes("AI Generated Notes").build());
        when(releaseRepository.save(any(Release.class))).thenReturn(release);

        pipelineService.generateReleaseNotes(1L);

        verify(groqService).generateReleaseNotes(argThat(req -> 
            req.getTasks().size() == 1 && req.getTasks().contains("Completed Task")
        ));
    }

    @Test
    void testUpdateReleaseNotes() {
        UpdateReleaseNotesRequest request = new UpdateReleaseNotesRequest();
        request.setReleaseNotes("Updated notes");

        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(releaseRepository.save(any(Release.class))).thenReturn(release);

        ApiResponse<ReleaseNoteResponse> response = pipelineService.updateReleaseNotes(1L, request);

        assertNotNull(response);
        assertEquals("Release notes updated successfully", response.getMessage());
        assertEquals("Updated notes", response.getData().getReleaseNotes());
    }

    @Test
    void testPublishRelease() {
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(release));
        when(releaseRepository.save(any(Release.class))).thenReturn(release);

        ApiResponse<ReleaseResponse> response = pipelineService.publishRelease(1L);

        assertNotNull(response);
        assertEquals("Release published successfully", response.getMessage());
        assertEquals("RELEASED", response.getData().getStatus());
        assertNotNull(response.getData().getReleasedAt());
    }
}
