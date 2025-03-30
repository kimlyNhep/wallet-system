package com.wlt.user.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class LongPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        String preHashed = preHash(rawPassword);
        return bcryptEncoder.encode(preHashed);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String preHashed = preHash(rawPassword);
        return bcryptEncoder.matches(preHashed, encodedPassword);
    }

    private String preHash(CharSequence rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    rawPassword.toString().getBytes(StandardCharsets.UTF_8)
            );
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Pre-hash failed", e);
        }
    }
}