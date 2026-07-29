package com.alura.finance_ai.finanzas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    //Debatir si hacer un ENUM  de tres opciones (Saludable/En observacion/En riesgo)
    // o recibir el String directo
    private String perfilFinanciero;

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
}
