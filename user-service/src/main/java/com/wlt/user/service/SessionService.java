package com.wlt.user.service;

import com.wlt.user.dto.LogoutResponseDto;
import com.wlt.user.dto.SessionResponseDto;

public interface SessionService {
    SessionResponseDto getSessionByToken(Long userId, String token);
    LogoutResponseDto removeSessionByToken(Long userId, String token);
}
