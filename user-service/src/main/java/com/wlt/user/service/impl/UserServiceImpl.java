package com.wlt.user.service.impl;

import com.wlt.user.constants.MessageEnum;
import com.wlt.user.constants.RoleName;
import com.wlt.user.dto.*;
import com.wlt.user.entity.Role;
import com.wlt.user.entity.User;
import com.wlt.user.exception.CustomException;
import com.wlt.user.repository.RoleRepository;
import com.wlt.user.repository.UserRepository;
import com.wlt.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleCacheService roleCacheService;

    @Override
    @SneakyThrows
    public UserRegisterResponseDto register(UserRegisterRequestDto userRegisterRequestDto) {
        Optional<User> user = userRepository.findByEmail(userRegisterRequestDto.getEmail());
        if (user.isPresent()) {
            throw new CustomException(MessageEnum.EMAIL_ALREADY_TAKEN);
        }

        User userEntity = new User();
        userEntity.setEmail(userRegisterRequestDto.getEmail());
        userEntity.setPasswordHash(passwordEncoder.encode(userRegisterRequestDto.getPassword()));
        userEntity.setEnabled(true);

        Optional<Role> userRole = roleCacheService.getRoles(RoleName.USER.name());
        if (userRole.isEmpty()) {
            throw new RuntimeException("Super admin role not found");
        }

        userEntity.setRoles(Set.of(userRole.get()));
        User userCreated = userRepository.save(userEntity);
        UserRegisterResponseDto response = new UserRegisterResponseDto();
        response.setUsername(userRegisterRequestDto.getEmail());
        response.setUserId(userCreated.getId());

        UserCreatedEvent userCreatedEvent = new UserCreatedEvent();
        userCreatedEvent.setUserId(userCreated.getId());

        return response;
    }

    @Override
    public GrantRoleResponseDto grantRole(Long adminUserId, GrantRoleRequestDto grantRoleRequestDto) {
        Optional<Role> superAdminRole = roleCacheService.getRoles(RoleName.SUPER_ADMIN.name());
        if (superAdminRole.isEmpty()) {
            throw new RuntimeException("Super admin role not found");
        }

        Optional<User> adminUser = userRepository.findByIdAndRoles(adminUserId, Set.of(superAdminRole.get()));
        if (adminUser.isEmpty()) {
            throw new RuntimeException("Admin user not found");
        }

        Optional<Role> userRole = roleCacheService.getRoles(RoleName.USER.name());

        if (userRole.isEmpty()) {
            throw new RuntimeException("User role not found");
        }

        Optional<User> userOptional = userRepository.findByIdAndRoles(grantRoleRequestDto.getUserId(), Set.of(userRole.get()));
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Optional<Role> newRole = roleCacheService.getRoles(grantRoleRequestDto.getRoleName());
        if (newRole.isEmpty()) {
            throw new RuntimeException("Role not found");
        }

        User user = userOptional.get();
        user.getRoles().add(newRole.get());
        userRepository.saveAndFlush(user);
        GrantRoleResponseDto response = new GrantRoleResponseDto();
        response.setRoleName(grantRoleRequestDto.getRoleName());
        response.setEmail(user.getEmail());
        return response;
    }
}
