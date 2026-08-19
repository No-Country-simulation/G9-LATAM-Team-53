package com.alura.finance_ai.finanzas.controller;

import com.alura.finance_ai.finanzas.dto.AnalisisFinancieroResponse;
import com.alura.finance_ai.finanzas.dto.IngresoMensualRequest;
import com.alura.finance_ai.finanzas.dto.IngresoMensualResponse;
import com.alura.finance_ai.finanzas.service.AnalisisFinancieroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/analisis-financiero")
public class AnalisisFinancieroController {

    private final AnalisisFinancieroService analisisService;

    public AnalisisFinancieroController(AnalisisFinancieroService analisisService) {
        this.analisisService = analisisService;
    }

    @GetMapping
    public ResponseEntity<?> obtenerAnalisisFinanciero(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            AnalisisFinancieroResponse response = analisisService.obtenerAnalisisFinanciero(userEmail);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Datos de entrada invalidos");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al procesar el análisis financiero");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PatchMapping("/ingreso-mensual")
    public ResponseEntity<?> actualizarIngresoMensual(@Valid @RequestBody IngresoMensualRequest request,
                                                      Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            IngresoMensualResponse response = analisisService.actualizarIngresoMensual(userEmail, request.ingresoMensual());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Datos de entrada invalidos");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar el ingreso mensual");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

}
