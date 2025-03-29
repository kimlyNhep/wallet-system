package com.wlt.user.controller;

import com.wlt.user.dto.SuccessResponse;
import com.wlt.user.dto.UserRegisterRequestDto;
import com.wlt.user.dto.UserRegisterResponseDto;
import com.wlt.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("api/v1/user/register")
    public ResponseEntity<SuccessResponse<UserRegisterResponseDto>> register(@RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        SuccessResponse<UserRegisterResponseDto> response = new SuccessResponse<>();
        response.setResponse(userService.register(userRegisterRequestDto));
        return ResponseEntity.ok(response);
    }
}
