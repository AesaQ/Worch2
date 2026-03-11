package com.worch.exceptions;

public class IdempotencyInterruptedException extends RuntimeException {
    public IdempotencyInterruptedException(String message) {
        super(message);
    }
}
