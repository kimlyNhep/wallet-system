package com.wlt.user.service.impl;

import com.wlt.user.dto.LogoutResponseDto;
import com.wlt.user.dto.SessionResponseDto;
import com.wlt.user.entity.Session;
import com.wlt.user.repository.SessionRepository;
import com.wlt.user.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SessionResponseDto getSessionByToken(Long userId, String token) {
        Optional<Session> session = sessionRepository.findByUserId(userId);
        if (session.isPresent()) {
            if (passwordEncoder.matches(token, session.get().getToken())) {
                SessionResponseDto sessionResponseDto = new SessionResponseDto();
                sessionResponseDto.setIp(session.get().getIp());
                sessionResponseDto.setToken(session.get().getToken());
                sessionResponseDto.setExpiresAt(session.get().getExpiresAt());
                return sessionResponseDto;
            }
        }

        throw new RuntimeException("session not found");
    }

    @Override
    public LogoutResponseDto removeSessionByToken(Long userId, String token) {
        Optional<Session> session = sessionRepository.findByUserId(userId);
        if (session.isPresent()) {
            if (passwordEncoder.matches(token, session.get().getToken())) {
                sessionRepository.delete(session.get());
            }

            LogoutResponseDto logoutResponseDto = new LogoutResponseDto();
            logoutResponseDto.setUserId(userId);
            return logoutResponseDto;
        }

        throw new RuntimeException("session not found");
    }
}
