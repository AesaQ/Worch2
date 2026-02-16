package com.worch.exceptions;

public class ChoiceOptionNotFoundException extends RuntimeException {
    public ChoiceOptionNotFoundException(String message) {
        super(message);
    }
}
