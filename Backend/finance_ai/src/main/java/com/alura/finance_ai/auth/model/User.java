package com.alura.finance_ai.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 20, message = "El nombre debe tener entre 2 y 20 caracteres")
    @Column(nullable = false, length = 20)
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 20, message = "El apellido debe tener entre 2 y 20 caracteres")
    @Column(nullable = false, length = 20)
    private String apellido;

    @NotBlank(message = "El email no puede estar vacío")
    @Column(nullable = false, unique = true, length = 255)
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 3)
    private String moneda;

    @Column(name = "ingreso_mensual", precision = 15, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(name = "nivel_endeudamiento")
    private Integer nivelEndeudamiento;

    @Column(name = "frecuencia_ahorro", length = 20)
    private String frecuenciaAhorro;
}
