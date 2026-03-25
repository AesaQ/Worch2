package com.worch.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.worch.exceptions.InvalidEnumValueException;
import java.util.Arrays;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum ChoiceStatus {
  ACTIVE,
  CLOSED,
  HIDDEN;

  @JsonCreator
  public static ChoiceStatus fromString(String status) {
    try {
      return ChoiceStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new InvalidEnumValueException(
          "Invalid status value: '" + status + "'. Supported values: " +
              String.join(", ", Arrays.stream(ChoiceStatus.values()).map(Enum::name).toList()),
          status,
          ChoiceStatus.class
      );
    }
  }

}