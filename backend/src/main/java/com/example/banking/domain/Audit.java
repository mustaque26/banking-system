package com.example.banking.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "audit")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OffsetDateTime timestamp;

    private String actor;

    private String action;

    private String entityType;

    private String entityId;

    @Column(length = 2000)
    private String details;

}

