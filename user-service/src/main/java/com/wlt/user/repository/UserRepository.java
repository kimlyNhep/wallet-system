package com.wlt.user.repository;

import com.wlt.user.entity.Role;
import com.wlt.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndRoles(Long id, Set<Role> roles);
}
