package com.forgemind.modules.ai.controller;

import com.forgemind.modules.ai.dto.request.AiGenerateTasksRequest;
import com.forgemind.modules.ai.dto.response.AiRiskAssessmentResponse;
import com.forgemind.modules.ai.dto.response.AiTaskSuggestionResponse;
import com.forgemind.modules.ai.dto.response.AiTextResponse;
import com.forgemind.modules.ai.service.AiOrchestratorService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiOrchestratorService aiOrchestratorService;

    @PostMapping("/projects/{projectId}/tasks/generate")
    public ResponseEntity<List<AiTaskSuggestionResponse>> generateTasks(
            @PathVariable UUID projectId,
            @Valid @RequestBody AiGenerateTasksRequest request) {
        return ResponseEntity.ok(aiOrchestratorService.generateTasks(projectId, request.getFeatureDescription()));
    }

    @GetMapping("/projects/{projectId}/summary")
    public ResponseEntity<AiTextResponse> summarizeProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(aiOrchestratorService.summarizeProject(projectId));
    }

    @GetMapping("/projects/{projectId}/risk-analysis")
    public ResponseEntity<List<AiRiskAssessmentResponse>> assessRisks(@PathVariable UUID projectId) {
        return ResponseEntity.ok(aiOrchestratorService.assessRisks(projectId));
    }

    @GetMapping("/projects/{projectId}/documentation/readme")
    public ResponseEntity<AiTextResponse> generateReadme(@PathVariable UUID projectId) {
        return ResponseEntity.ok(aiOrchestratorService.generateReadme(projectId));
    }
}
