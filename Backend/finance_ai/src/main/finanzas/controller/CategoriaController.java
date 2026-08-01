package com.alura.finance_ai.finanzas.controller;

import com.alura.finance_ai.finanzas.entity.Categoria;
import com.alura.finance_ai.finanzas.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<?> listarCategorias() {

        try {

            List<Categoria> categorias = categoriaService.listarCategorias();

            return ResponseEntity.ok(categorias);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar categorías: " + e.getMessage());

        }

    }

    @PostMapping
    public ResponseEntity<?> insertarCategoria(@Valid @RequestBody Categoria categoria) {

        try {

            Categoria nuevaCategoria = categoriaService.guardarCategoria(categoria);

            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al guardar la categoría: " + e.getMessage());

        }

    }

}