package com.ellu.looper.repository;

import com.ellu.looper.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Custom query to find a RefreshToken by its associated member ID
    Optional<RefreshToken> findByUserId(Long userId);

    // Optional custom query to delete a RefreshToken by member ID
    void deleteByUserId(Long userId);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
