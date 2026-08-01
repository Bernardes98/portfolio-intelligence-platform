package com.portfoliointelligence.exception;

public class DocumentProcessingException
        extends RuntimeException {

    public DocumentProcessingException(String message) {
        super(message);
    }

    public DocumentProcessingException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}