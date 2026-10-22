package com.aivideoeraser.exception;

import org.springframework.http.HttpStatus;

public class InsufficientCreditsException extends AppException {
    public InsufficientCreditsException(int required, int available) {
        super(String.format("Insufficient credits: Required %d credits, but account has only %d credits. Please upgrade or subscribe.", required, available), HttpStatus.PAYMENT_REQUIRED);
    }
}
