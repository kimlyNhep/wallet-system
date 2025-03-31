package com.wlt.user.service.impl;

import com.wlt.user.constants.MessageError;
import com.wlt.user.constants.RoleName;
import com.wlt.user.dto.*;
import com.wlt.user.entity.Role;
import com.wlt.user.entity.User;
import com.wlt.user.exception.CustomException;
import com.wlt.user.repository.UserRepository;
import com.wlt.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

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
            throw new CustomException(MessageError.EMAIL_ALREADY_TAKEN);
        }

        final String EMAIL_REGEX =
                "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

        if (userRegisterRequestDto.getEmail() == null || userRegisterRequestDto.getEmail().isEmpty()) {
            throw new CustomException(MessageError.EMAIL_CANNOT_BE_NULL);
        }

        if (!PATTERN.matcher(userRegisterRequestDto.getEmail()).matches()) {
            throw new CustomException(MessageError.EMAIL_INVALID);
        }

        User userEntity = new User();
        userEntity.setEmail(userRegisterRequestDto.getEmail());
        userEntity.setPasswordHash(passwordEncoder.encode(userRegisterRequestDto.getPassword()));
        userEntity.setEnabled(true);

        Optional<Role> userRole = roleCacheService.getRoles(RoleName.USER.name());
        if (userRole.isEmpty()) {
            throw new CustomException(MessageError.ROLE_NOT_FOUND);
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
            throw new CustomException(MessageError.SUPER_ADMIN_NOT_FOUND);
        }

        Optional<User> adminUser = userRepository.findByIdAndRoles(adminUserId, Set.of(superAdminRole.get()));
        if (adminUser.isEmpty()) {
            throw new CustomException(MessageError.USER_NOT_FOUND);
        }

        Optional<Role> userRole = roleCacheService.getRoles(RoleName.USER.name());

        if (userRole.isEmpty()) {
            throw new CustomException(MessageError.ROLE_NOT_FOUND);
        }

        Optional<User> userOptional = userRepository.findByIdAndRoles(grantRoleRequestDto.getUserId(), Set.of(userRole.get()));
        if (userOptional.isEmpty()) {
            throw new CustomException(MessageError.USER_NOT_FOUND);
        }

        Optional<Role> newRole = roleCacheService.getRoles(grantRoleRequestDto.getRoleName());
        if (newRole.isEmpty()) {
            throw new CustomException(MessageError.ROLE_NOT_FOUND);
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
