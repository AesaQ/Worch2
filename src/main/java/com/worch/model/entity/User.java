package com.worch.model.entity;

import com.worch.model.enums.Language;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column
    private String phone;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false, name = "password")
    private String password;

    @Column
    private String email;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private OffsetDateTime birthday;

    @Enumerated(EnumType.STRING)
    @Column
    private Language language;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
