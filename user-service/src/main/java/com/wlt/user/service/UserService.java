package com.wlt.user.service;

import com.wlt.user.dto.UserRegisterRequestDto;
import com.wlt.user.dto.UserRegisterResponseDto;

public interface UserService {
    UserRegisterResponseDto register(UserRegisterRequestDto userRegisterRequestDto);
}
