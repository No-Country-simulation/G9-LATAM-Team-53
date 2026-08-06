package com.alura.finance_ai.finanzas.controller;

import com.alura.finance_ai.finanzas.dto.TransaccionRequest;
import com.alura.finance_ai.finanzas.dto.TransaccionResponse;
import com.alura.finance_ai.finanzas.service.TransaccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @PostMapping
    public ResponseEntity<?> crearTransaccion(@Valid @RequestBody TransaccionRequest request,
                                              Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            TransaccionResponse response = transaccionService.registrarTransaccion(request, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Datos de entrada invalidos");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno al procesar la transaccion");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
