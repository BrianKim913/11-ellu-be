package com.ellu.looper.service;

import com.ellu.looper.dto.schedule.ScheduleCreateRequest;
import com.ellu.looper.dto.schedule.ScheduleResponse;
import com.ellu.looper.dto.schedule.ScheduleUpdateRequest;
import com.ellu.looper.entity.Schedule;
import com.ellu.looper.entity.User;
import com.ellu.looper.repository.ScheduleRepository;
import com.ellu.looper.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;
  private final UserRepository memberRepository;

  private void validateTimeOrder(LocalDateTime startTime, LocalDateTime endTime) {
    if (endTime.isEqual(startTime) || endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("종료 시각은 시작 시각보다 나중이어야 합니다.");
    }
  }

  public ScheduleResponse createSchedule(Long memberId, ScheduleCreateRequest request) {
    User user = memberRepository.findById(memberId)
        .orElseThrow(() -> new AccessDeniedException("unauthorized"));
    
    validateTimeOrder(request.startTime(), request.endTime());
    
    Schedule schedule = Schedule.builder()
        .user(user)
        .title(request.title())
        .description(request.description())
        .isAiRecommended(request.aiRecommended())
        .isCompleted(false)
        .startTime(request.startTime())
        .endTime(request.endTime())
        .build();
    Schedule saved = scheduleRepository.save(schedule);
    return toResponse(saved, false);
  }


  public ScheduleResponse updateSchedule(Long memberId, Long id, ScheduleUpdateRequest request) {
    Schedule existing = scheduleRepository.findByIdAndUserIdAndDeletedAtIsNull(id, memberId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid or already deleted schedule"));

    LocalDateTime newStart = request.startTime() != null ? request.startTime() : existing.getStartTime();
    LocalDateTime newEnd = request.endTime() != null ? request.endTime() : existing.getEndTime();

    validateTimeOrder(newStart, newEnd);

    Schedule updated = existing.toBuilder()
        .title(request.title() != null ? request.title() : existing.getTitle())
        .description(
            request.description() != null ? request.description() : existing.getDescription())
        .isCompleted(request.completed() != null ? request.completed() : existing.isCompleted())
        .startTime(request.startTime()!=null?request.startTime():existing.getStartTime())
        .endTime(request.endTime() != null ? request.endTime() : existing.getEndTime())
        .build();

    return toResponse(scheduleRepository.save(updated), false);
  }


  public void deleteSchedule(Long memberId, Long id) {
    Schedule schedule = scheduleRepository.findByIdAndUserIdAndDeletedAtIsNull(id, memberId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid or already deleted schedule"));
    schedule.toBuilder().deletedAt(LocalDateTime.now()).build();
    scheduleRepository.delete(schedule);
  }

  public List<ScheduleResponse> getDailySchedules(Long memberId, LocalDate date) {
    LocalDateTime start = date.atStartOfDay();
    LocalDateTime end = date.plusDays(1).atStartOfDay();

    List<Schedule> personalSchedules = scheduleRepository.findDailySchedules(memberId, start, end);

    List<ScheduleResponse> responses = personalSchedules.stream()
        .map(s -> toResponse(s, false))
        .collect(Collectors.toList());

    List<ScheduleResponse> projectSchedules = getProjectSchedules(memberId, start, end);

    responses.addAll(projectSchedules);
    return responses;
  }


  public Map<LocalDate, List<ScheduleResponse>> getSchedulesByRange(Long memberId,
      LocalDate startDate, LocalDate endDate) {
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1); // 범위 끝을 포함시키기 위해

    List<Schedule> personal = scheduleRepository.findSchedulesBetween(memberId, start, end);
    List<ScheduleResponse> responses = personal.stream()
        .map(s -> toResponse(s, false)).toList();

    List<ScheduleResponse> project = getProjectSchedules(memberId, startDate.atStartOfDay(),
        endDate.plusDays(1).atStartOfDay());

    List<ScheduleResponse> all = new ArrayList<>();
    all.addAll(responses);
    all.addAll(project);

    return all.stream().collect(Collectors.groupingBy(r -> r.startTime().toLocalDate()));
  }

  private List<ScheduleResponse> getProjectSchedules(Long memberId, LocalDateTime start,
      LocalDateTime end) {
    // ProjectScheduleService 에서 가져온다고 가정
    return List.of(); // 예시
  }

  private ScheduleResponse toResponse(Schedule s, boolean isProject) {
    return ScheduleResponse.builder()
        .title(s.getTitle())
        .description(s.getDescription())
        .completed(s.isCompleted())
        .aiRecommended(s.isAiRecommended())
        .projectSchedule(isProject)
        .startTime(s.getStartTime())
        .endTime(s.getEndTime())
        .createdAt(s.getCreatedAt())
        .updatedAt(s.getUpdatedAt())
        .build();
  }
}
