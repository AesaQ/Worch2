package com.worch.exceptions;

public class ChoiceExpiredException extends RuntimeException {
  public ChoiceExpiredException(String message) {
    super(message);
  }
  public ChoiceExpiredException() {}
}
