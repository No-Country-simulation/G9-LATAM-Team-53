package com.alura.finance_ai.finanzas.controller;

import com.alura.finance_ai.finanzas.dto.CategoriaRequest;
import com.alura.finance_ai.finanzas.dto.CategoriaResponse;
import com.alura.finance_ai.finanzas.model.Categoria;
import com.alura.finance_ai.finanzas.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<?> listarCategorias() {
        try {
            List<CategoriaResponse> categorias = categoriaService.listarCategorias()
                    .stream()
                    .map(this::mapearRespuesta)
                    .toList();
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar categorias: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> insertarCategoria(@Valid @RequestBody CategoriaRequest request) {
        try {
            Categoria categoria = new Categoria(null, request.nombre());
            Categoria nuevaCategoria = categoriaService.guardarCategoria(categoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapearRespuesta(nuevaCategoria));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al guardar la categoria: " + e.getMessage());
        }
    }

    private CategoriaResponse mapearRespuesta(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre());
    }
}
