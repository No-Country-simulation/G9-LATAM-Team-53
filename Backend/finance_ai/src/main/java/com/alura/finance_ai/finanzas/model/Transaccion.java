package com.alura.finance_ai.finanzas.model;

import com.alura.finance_ai.auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "transacciones",
        indexes = {
                @Index(name = "idx_user", columnList = "user_id"),
                @Index(name = "idx_fecha", columnList = "fecha")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion")
    private Long idTransaccion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activa = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisis_id")
    private AnalisisFinanciero analisisFinanciero;

    private LocalDate fecha;

    @Column(precision = 15, scale = 2)
    private BigDecimal valor;
}
