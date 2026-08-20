package com.alura.finance_ai.finanzas.model;

import com.alura.finance_ai.auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "analisis_financiero")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalisisFinanciero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_financiero")
    private PerfilFinanciero perfilFinanciero;

    private Double probabilidad;

    @ElementCollection
    @CollectionTable(name = "resumen_gastos", joinColumns = @JoinColumn(name = "analisis_id"))
    @MapKeyColumn(name = "categoria")
    @Column(name = "valor")
    private Map<String, Double> resumenGastos;

    @ElementCollection
    @CollectionTable(name = "recomendaciones", joinColumns = @JoinColumn(name = "analisis_id"))
    @Column(name = "recomendacion")
    private List<String> recomendaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    @OneToMany(mappedBy = "analisisFinanciero", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Transaccion> transacciones = new ArrayList<>();
}
