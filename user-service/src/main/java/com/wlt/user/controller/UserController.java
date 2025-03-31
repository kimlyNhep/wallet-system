package com.wlt.user.controller;

import com.wlt.user.dto.*;
import com.wlt.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/api/v1/user/register")
    public ResponseEntity<SuccessResponse<UserRegisterResponseDto>> register(@RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        SuccessResponse<UserRegisterResponseDto> response = new SuccessResponse<>();
        response.setResponse(userService.register(userRegisterRequestDto));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/user/grant/role")
    public ResponseEntity<SuccessResponse<GrantRoleResponseDto>> grantRole(
            @RequestHeader("user-id") Long adminUserId,
            @RequestBody GrantRoleRequestDto grantRoleRequestDto
    ) {
        SuccessResponse<GrantRoleResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setCode("SUCCESS");
        successResponse.setMessage("success");
        successResponse.setResponse(userService.grantRole(adminUserId, grantRoleRequestDto));
        return ResponseEntity.ok(successResponse);
    }
}
