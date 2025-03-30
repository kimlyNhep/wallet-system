package com.wlt.user.service.impl;

import com.wlt.user.entity.Role;
import com.wlt.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleCacheService {

    private final RoleRepository roleRepository;

    @Cacheable(value = "roles", key = "#roleName")
    public Optional<Role> getRoles(String roleName) {
        return roleRepository.findByName(roleName);
    }
}
