package com.portfoliointelligence.exception;

public class DocumentLimitExceededException
        extends RuntimeException {

    public DocumentLimitExceededException(String message) {
        super(message);
    }
}