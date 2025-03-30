package com.wlt.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wlt_session")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String token;
    private String ip;
    private String status;
    private LocalDateTime expiresAt;
    private String userAgent;
    private LocalDateTime createdAt;
    private String createdBy;
}
