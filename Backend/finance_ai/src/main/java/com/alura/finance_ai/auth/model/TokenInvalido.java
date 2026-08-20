package com.alura.finance_ai.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tokens_invalidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenInvalido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String token;

    // Metodo para mayor legibilidad
    public static TokenInvalido of(String token) {
        return new TokenInvalido(null, token);
    }
}
