package com.ellu.looper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ProjectListResponse {
    private String title;
    private String color;
    private List<ProjectMemberResponse> members;

}
