package com.worch.model.entity;

import com.worch.model.enums.ExpertApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "expert_application")
public class ExpertApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column
    private String motivation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpertApplicationStatus status;

    @Column(nullable = false)
    private OffsetDateTime submittedAt;
}
