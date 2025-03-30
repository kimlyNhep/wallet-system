package com.wlt.user.repository;

import com.wlt.user.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByUserId(Long userId);
    Optional<Session> findByUserIdAndStatus(Long userId, String status);
}
