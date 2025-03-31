package com.wlt.user.service;

import com.wlt.user.dto.GrantRoleRequestDto;
import com.wlt.user.dto.GrantRoleResponseDto;
import com.wlt.user.dto.UserRegisterRequestDto;
import com.wlt.user.dto.UserRegisterResponseDto;

public interface UserService {
    UserRegisterResponseDto register(UserRegisterRequestDto userRegisterRequestDto);
    GrantRoleResponseDto grantRole(Long adminUserId, GrantRoleRequestDto grantRoleRequestDto);
}
