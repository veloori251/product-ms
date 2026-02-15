package com.example.product.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp,
        List<FieldValidationError> errors
) {}
