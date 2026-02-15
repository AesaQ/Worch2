package com.worch.exceptions;

import java.util.UUID;

public class ChoiceOptionMismatchException extends RuntimeException {
    public ChoiceOptionMismatchException(UUID choiceId, UUID choiceOptionId) {
        super("choice option mismatch: choiceId " + choiceId.toString() + " and choiceOptionId" + choiceOptionId.toString());
    }
}
