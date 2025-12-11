package com.worch.model.entity;


import com.worch.model.enums.ChoiceStatus;
import com.worch.model.enums.converter.ChoiceStatusConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "choice")
@Getter
@Setter
public class Choice {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @ManyToOne
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @Column(length = 300)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String imageLink;

  private Boolean personal;

  @Convert(converter = ChoiceStatusConverter.class)
  private ChoiceStatus status;

  private OffsetDateTime deadline;

  @CreationTimestamp
  @Column(nullable = false)
  private OffsetDateTime createdAt;
}
