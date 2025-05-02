package com.ellu.looper.dto.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record ProjectScheduleCreateResponse(
    String message,
    Data data
) {

  public record Data(
      List<ProjectScheduleDto> scheduled
  ) {

    public record ProjectScheduleDto(

        String title,

        @JsonProperty("start_time") Instant startTime,

        @JsonProperty("end_time") Instant endTime,

        @JsonProperty("is_completed") boolean completed
    ) {

    }
  }
}