package com.alura.finance_ai.finanzas.service;

import com.alura.finance_ai.finanzas.entity.Categoria;
import com.alura.finance_ai.finanzas.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    @Override
    public Categoria guardarCategoria(Categoria categoria) {

        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            throw new RuntimeException("La categoría ya existe.");
        }

        return categoriaRepository.save(categoria);
    }

}