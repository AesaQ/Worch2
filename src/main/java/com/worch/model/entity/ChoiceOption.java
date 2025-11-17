package com.worch.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "choice_option")
@Setter
@Getter
public class ChoiceOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "choice_id")
    private Choice choice;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer position;
}
