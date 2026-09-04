package com.recruitmentplatform.auth.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }

    public ConflictException(String message, String code) {
        super(message, HttpStatus.CONFLICT, code);
    }
}
