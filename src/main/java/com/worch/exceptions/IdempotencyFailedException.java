package com.worch.exceptions;

public class IdempotencyFailedException extends RuntimeException {
    public IdempotencyFailedException(String message) {
        super(message);
    }
}
