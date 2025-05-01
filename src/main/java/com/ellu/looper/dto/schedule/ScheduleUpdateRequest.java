package com.ellu.looper.dto.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ScheduleUpdateRequest(

    String title,

    String description,

    @JsonProperty("isCompleted")
    Boolean completed,

    LocalDateTime startTime,

    LocalDateTime endTime
) {

}

