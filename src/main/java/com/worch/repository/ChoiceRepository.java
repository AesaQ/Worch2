package com.worch.repository;

import com.worch.model.entity.Choice;

import java.util.List;
import java.util.UUID;

import com.worch.model.enums.ChoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoiceRepository extends JpaRepository<Choice, UUID>,
    JpaSpecificationExecutor<Choice> {
    List<Choice> getAllByCreatorId(UUID creatorId);
    List<Choice> findByStatus(ChoiceStatus status);
    List<Choice> getAllByCreatorIdAndStatus(UUID creatorId, ChoiceStatus status);
}
