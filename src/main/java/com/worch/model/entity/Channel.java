package com.worch.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@NamedEntityGraph(
        name = "Channel.withMembers",
        attributeNodes = @NamedAttributeNode(value = "members",
                subgraph = "membersWithUserAndRole"),
        subgraphs = @NamedSubgraph(
                name = "membersWithUserAndRole",
                attributeNodes = {@NamedAttributeNode("user"), @NamedAttributeNode("role")}
        )
)
@Entity
@Table(name = "channel")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, name = "id")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private Boolean isPrivate;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean ageRestricted;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
    private Set<ChannelUser> members;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}

