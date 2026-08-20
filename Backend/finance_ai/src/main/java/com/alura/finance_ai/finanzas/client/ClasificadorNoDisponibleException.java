package com.alura.finance_ai.finanzas.client;

public class ClasificadorNoDisponibleException extends RuntimeException {

    public ClasificadorNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClasificadorNoDisponibleException(String message) {
        super(message);
    }
}
