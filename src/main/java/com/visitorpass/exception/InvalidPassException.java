package com.visitorpass.exception;

public class InvalidPassException extends RuntimeException {
    public InvalidPassException(String message) {
        super(message);
    }
}
