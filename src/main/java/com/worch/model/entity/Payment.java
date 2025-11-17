package com.worch.model.entity;

import com.worch.model.converter.PaymentMetadataConverter;
import com.worch.model.dto.metadata.PaymentMetadata;
import com.worch.model.enums.CurrencyEnum;
import com.worch.model.enums.PaymentStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @Positive
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    private CurrencyEnum currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatusEnum status;

    private String description;

    private String clientEmail;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = PaymentMetadataConverter.class)
    private PaymentMetadata metadata;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    private OffsetDateTime paidAt;

    private OffsetDateTime cancelledAt;
}

