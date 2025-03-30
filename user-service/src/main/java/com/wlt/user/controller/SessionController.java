package com.wlt.user.controller;

import com.wlt.user.dto.LogoutResponseDto;
import com.wlt.user.dto.SessionResponseDto;
import com.wlt.user.dto.SuccessResponse;
import com.wlt.user.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/api/v1/session")
    public ResponseEntity<SuccessResponse<SessionResponseDto>> getCurrentSession(
            @RequestHeader("user-id") Long userId,
            @RequestHeader("token") String token
    ) {
        SuccessResponse<SessionResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setMessage("success");
        successResponse.setResponse(sessionService.getSessionByToken(userId, token));

        return ResponseEntity.ok(successResponse);
    }

    @GetMapping("/api/v1/logout")
    public ResponseEntity<SuccessResponse<LogoutResponseDto>> logout(
            @RequestHeader("user-id") Long userId,
            @RequestHeader("token") String token
    ) {
        SuccessResponse<LogoutResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setMessage("success");
        successResponse.setResponse(sessionService.removeSessionByToken(userId, token));

        return ResponseEntity.ok(successResponse);
    }
}
