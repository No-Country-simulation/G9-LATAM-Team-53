package com.alura.finance_ai.finanzas.service;

import com.alura.finance_ai.finanzas.entity.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaService {

    List<Categoria> listarCategorias();

    Optional<Categoria> buscarPorId(Long id);

    Categoria guardarCategoria(Categoria categoria);

}