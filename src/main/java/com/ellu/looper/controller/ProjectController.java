package com.ellu.looper.controller;

import com.ellu.looper.commons.ApiResponse;
import com.ellu.looper.dto.ProjectCreateRequest;
import com.ellu.looper.dto.ProjectResponse;
import com.ellu.looper.dto.WikiRequest;
import com.ellu.looper.service.AiService;
import com.ellu.looper.service.ProjectService;
import com.ellu.looper.commons.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final AiService aiService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createProject(
        @CurrentUser Long userId,
        @RequestBody ProjectCreateRequest request) {
        projectService.createProject(request, userId);
        return ResponseEntity.ok(ApiResponse.success("project_created", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(@CurrentUser Long userId) {
        List<ProjectResponse> responses = projectService.getProjects(userId);
        return ResponseEntity.ok(ApiResponse.success("project_list", responses));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectDetails(
        @CurrentUser Long userId,
        @PathVariable Long projectId) {
        ProjectResponse response = projectService.getProjectDetail(projectId, userId);
        return ResponseEntity.ok(ApiResponse.success("project_fetched", response));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
        @CurrentUser Long userId,
        @PathVariable Long projectId,
        @RequestBody ProjectCreateRequest request) {
        ProjectResponse updated = null;
            projectService.updateProject(projectId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("project_updated", updated));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
        @CurrentUser Long userId,
        @PathVariable Long projectId) {
        projectService.deleteProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/wiki")
    public ResponseEntity<ApiResponse<?>> createWiki(
        @CurrentUser Long userId,
        @PathVariable Long projectId,
        @RequestBody WikiRequest wikiRequest) {
        aiService.createProjectWiki(projectId, wikiRequest);
        return ResponseEntity.status(201).body(ApiResponse.success("wiki_created", null));
    }

    @GetMapping("/{projectId}/wiki")
    public ResponseEntity<ApiResponse<?>> getWiki(
        @CurrentUser Long userId,
        @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success("wiki_fetched", aiService.getProjectWiki(projectId)));
    }

    @PatchMapping("/{projectId}/wiki")
    public ResponseEntity<ApiResponse<?>> updateWiki(
        @CurrentUser Long userId,
        @PathVariable Long projectId,
        @RequestBody WikiRequest wikiRequest) {
        aiService.updateProjectWiki(projectId, wikiRequest);
        return ResponseEntity.ok(ApiResponse.success("wiki_updated", null));
    }
}
