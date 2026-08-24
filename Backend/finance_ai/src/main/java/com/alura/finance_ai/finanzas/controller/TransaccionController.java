package com.alura.finance_ai.finanzas.controller;

import com.alura.finance_ai.finanzas.client.ClasificadorNoDisponibleException;
import com.alura.finance_ai.finanzas.dto.ActualizarCategoriaTransaccionRequest;
import com.alura.finance_ai.finanzas.dto.TransaccionRequest;
import com.alura.finance_ai.finanzas.dto.TransaccionResponse;
import com.alura.finance_ai.finanzas.service.TransaccionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Validated
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
        } catch (ClasificadorNoDisponibleException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Servicio de clasificacion no disponible");
            error.put("mensaje", "No se pudo clasificar la transaccion. Intente nuevamente.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
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

    @GetMapping
    public ResponseEntity<?> listarTransacciones(Authentication authentication,
                                                 @RequestParam(defaultValue = "0") int pagina,
                                                 @RequestParam(defaultValue = "10") int tamanio) {
        if (pagina < 0 || tamanio < 1 || tamanio > 100) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Datos de entrada invalidos",
                    "mensaje", "La pagina debe ser mayor o igual a cero y el tamanio entre 1 y 100"
            ));
        }
        try {
            Page<TransaccionResponse> response = transaccionService.listarTransacciones(authentication.getName(), pagina, tamanio);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Datos de entrada invalidos",
                    "mensaje", e.getMessage()
            ));
        }
    }

    @PatchMapping("/{idTransaccion}/categoria")
    public ResponseEntity<?> actualizarCategoriaTransaccion(@PathVariable @Positive Long idTransaccion,
                                                            @Valid @RequestBody ActualizarCategoriaTransaccionRequest request,
                                                            Authentication authentication) {
        try {
            TransaccionResponse response = transaccionService.actualizarCategoriaTransaccion(
                    idTransaccion,
                    request,
                    authentication.getName()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Datos de entrada invalidos",
                    "mensaje", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error interno al actualizar la categoria",
                    "mensaje", e.getMessage()
            ));
        }
    }
}
