package com.ellu.looper.repository;

import com.ellu.looper.entity.Project;
import com.ellu.looper.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByMember(User member);

    Optional<Project> findByIdAndDeletedAtIsNull(Long projectId);
}
