package com.ellu.looper.dto.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record ProjectScheduleCreateRequest(
    @NotEmpty(message = "project_schedules must not be empty")
    List<ProjectScheduleDto> project_schedules
) {
  public record ProjectScheduleDto(
      @NotBlank(message = "title is required")
      String title,

      @NotNull(message = "start_time is required")
      @JsonProperty("start_time")
      Instant startTime,

      @NotNull(message = "end_time is required")
      @JsonProperty("end_time")
      Instant endTime,

      @NotNull(message = "is_completed is required")
      @JsonProperty("is_completed")
      Boolean isCompleted
  ) {}
}

