package com.worch.model.entity;

import com.worch.model.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "idempotency_key")
public class IdempotencyKey {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    @Column(name = "idem_key", nullable = false)
    private String idemKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_status")
    private IdempotencyStatus responseStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
