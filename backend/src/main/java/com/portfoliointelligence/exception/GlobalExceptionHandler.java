package com.portfoliointelligence.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                exception.getMessage(),
                "urn:problem-type:resource-not-found",
                request
        );
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail handleConflict(
            ConflictException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.CONFLICT,
                "Conflict",
                exception.getMessage(),
                "urn:problem-type:conflict",
                request
        );
    }

    @ExceptionHandler(InvalidDocumentException.class)
    ProblemDetail handleInvalidDocument(
            InvalidDocumentException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid document",
                exception.getMessage(),
                "urn:problem-type:invalid-document",
                request
        );
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    ProblemDetail handleDuplicateDocument(
            DuplicateDocumentException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.CONFLICT,
                "Duplicate document",
                exception.getMessage(),
                "urn:problem-type:duplicate-document",
                request
        );
    }

    @ExceptionHandler({
            DocumentLimitExceededException.class,
            InvalidAnalysisStatusException.class
    })
    ProblemDetail handleUnprocessableEntity(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Document upload not allowed",
                exception.getMessage(),
                "urn:problem-type:document-upload-not-allowed",
                request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail handleMaxUploadSize(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Upload too large",
                "The upload exceeds the permitted request size.",
                "urn:problem-type:upload-too-large",
                request
        );
    }

    @ExceptionHandler(FileStorageException.class)
    ProblemDetail handleFileStorage(
            FileStorageException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "File storage error",
                exception.getMessage(),
                "urn:problem-type:file-storage-error",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Validation error",
                "One or more fields are invalid.",
                "urn:problem-type:validation-error",
                request
        );

        List<FieldValidationError> errors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                new FieldValidationError(
                                        error.getField(),
                                        error.getDefaultMessage()
                                )
                        )
                        .toList();

        problem.setProperty("errors", errors);

        return problem;
    }

    private static ProblemDetail createProblem(
            HttpStatus status,
            String title,
            String detail,
            String type,
            HttpServletRequest request
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        status,
                        detail
                );

        problem.setTitle(title);
        problem.setType(URI.create(type));
        problem.setInstance(
                URI.create(request.getRequestURI())
        );
        problem.setProperty(
                "timestamp",
                Instant.now()
        );

        return problem;
    }

    private record FieldValidationError(
            String field,
            String message
    ) {
    }
}