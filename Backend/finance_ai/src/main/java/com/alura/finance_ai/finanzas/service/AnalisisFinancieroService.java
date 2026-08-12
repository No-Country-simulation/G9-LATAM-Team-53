package com.alura.finance_ai.finanzas.service;

import com.alura.finance_ai.auth.model.User;
import com.alura.finance_ai.auth.repository.UserRepository;
import com.alura.finance_ai.finanzas.dto.AnalisisFinancieroResponse;
import com.alura.finance_ai.finanzas.dto.IngresoMensualResponse;
import com.alura.finance_ai.finanzas.repository.AnalisisFinancieroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
public class AnalisisFinancieroService {

    private final UserRepository userRepository;
    private final AnalisisFinancieroRepository analisisFinancieroRepository;

    public AnalisisFinancieroService(UserRepository userRepository, AnalisisFinancieroRepository analisisFinancieroRepository) {
        this.userRepository = userRepository;
        this.analisisFinancieroRepository = analisisFinancieroRepository;
    }

    public AnalisisFinancieroResponse obtenerAnalisisFinanciero(String userEmail) {
        // TAREA PENDIENTE DEL EQUIPO (Punto 3)
        return null;
    }

    @Transactional
    public IngresoMensualResponse actualizarIngresoMensual(String userEmail, BigDecimal nuevoIngresoMensual) {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setIngresoMensual(nuevoIngresoMensual);
        User usuarioActualizado = userRepository.save(usuario);

        return new IngresoMensualResponse(
                usuarioActualizado.getUserId(),
                usuarioActualizado.getIngresoMensual(),
                "Ingreso mensual actualizado correctamente"
        );
    }
}