package com.casanova.aprendendospring.infrastructure.exceptions;

public class ConflictException extends RuntimeException {

    // Construtor simples
    public ConflictException(String message) {
        super(message);
    }

    // Construtor com causa (útil para rastrear erros)
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    // Método de fábrica estático
    public static ConflictException createConflictException(String message) {
        return new ConflictException(message);
    }
}
