package com.worch.service;

import com.worch.exceptions.ChoiceNotFoundException;
import com.worch.exceptions.StackNotFoundException;
import com.worch.model.entity.Choice;
import com.worch.model.entity.Stack;
import com.worch.model.entity.StackItem;
import com.worch.repository.ChoiceRepository;
import com.worch.repository.StackItemRepository;
import com.worch.repository.StackRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StackService {

  private final StackRepository stackRepository;
  private final ChoiceRepository choiceRepository;
  private final StackItemRepository stackItemRepository;

  public Page<Stack> getAllStacks(Pageable pageable) {
    return stackRepository.findAll(pageable);
  }

  @Transactional
  public void addChoiceToStack(UUID stackId, UUID choiceId) {
    var choice = getChoiceOrThrow(choiceId);

    var stack = getStackOrThrow(stackId);

    var stackItem = new StackItem();
    stackItem.setStack(stack);
    stackItem.setChoice(choice);
    stackItem.setPosition(null);

    stackItemRepository.save(stackItem);
  }

  @Transactional
  public void deleteChoiceFromStack(UUID stackId, UUID choiceId) {
    var stackItem = stackItemRepository
        .findByStackIdAndChoiceId(stackId, choiceId)
        .orElseThrow(
            () -> new IllegalArgumentException("Выбранный choice не принадлежит указанному stack"));

    stackItemRepository.delete(stackItem);
  }

  public Choice getChoiceOrThrow(UUID choiceId) {
    return choiceRepository
        .findById(choiceId)
        .orElseThrow(() -> new ChoiceNotFoundException("Choice не найден"));
  }

  public Stack getStackOrThrow(UUID stackId) {
    return stackRepository
        .findById(stackId)
        .orElseThrow(() -> new StackNotFoundException("Stack не найден"));
  }
}