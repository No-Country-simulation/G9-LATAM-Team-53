package com.alura.finance_ai.finanzas.controller;

import com.alura.finance_ai.finanzas.dto.IngresoMensualRequest;
import com.alura.finance_ai.finanzas.dto.AnalisisFinancieroResponse;
import com.alura.finance_ai.finanzas.service.AnalisisFinancieroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class AnalisisFinancieroController {

    private final AnalisisFinancieroService analisisService;

    public AnalisisFinancieroController(AnalisisFinancieroService analisisService) {
        this.analisisService = analisisService;
    }

    /**
     * Endpoint para consultar el análisis financiero del usuario autenticado.
     * GET /analisis-financiero
     */
    @GetMapping("/analisis-financiero")
    public ResponseEntity<?> obtenerAnalisisFinanciero(Authentication authentication) {
        try {
            String userEmail = authentication.getName();

            AnalisisFinancieroResponse response = analisisService.obtenerAnalisisFinanciero(userEmail);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sin datos suficientes");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al procesar el análisis financiero");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint para actualizar o setear el ingreso mensual del usuario.
     * PATCH /ingresomensual
     */

}