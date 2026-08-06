package com.alura.finance_ai.auth.dto;

public record AuthResponse(
        String token,
        Long userId,
        String nombre,
        String apellido,
        String email
) {
}
