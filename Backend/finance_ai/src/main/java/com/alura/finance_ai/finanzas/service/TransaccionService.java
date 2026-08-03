package com.alura.finance_ai.finanzas.service;

import com.alura.finance_ai.auth.model.User;
import com.alura.finance_ai.auth.repository.UserRepository;
import com.alura.finance_ai.finanzas.dto.TransaccionRequest;
import com.alura.finance_ai.finanzas.dto.TransaccionResponse;
import com.alura.finance_ai.finanzas.model.Transaccion;
import com.alura.finance_ai.finanzas.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final UserRepository userRepository;

    public TransaccionService(TransaccionRepository transaccionRepository, UserRepository userRepository) {
        this.transaccionRepository = transaccionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransaccionResponse registrarTransaccion(TransaccionRequest request, String userEmail) {
        // 1. Buscar al usuario autenticado mediante el email del JWT
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Determinar categoría (Si no la envía, se asigna una categoría por defecto o clasificada por IA)
        String categoriaAsignada = (request.categoria() != null && !request.categoria().isBlank())
                ? request.categoria()
                : "Otros";

        // 3. Crear y guardar la entidad
        Transaccion transaccion = Transaccion.builder()
                .descripcion(request.descripcion())
                .valor(request.valor())
                .categoria(categoriaAsignada)
                .fecha(request.fecha())
                .usuario(usuario)
                .activa(true)
                .build();

        Transaccion guardada = transaccionRepository.save(transaccion);

        // 4. Retornar DTO de respuesta
        return new TransaccionResponse(
                guardada.getId(),
                guardada.getDescripcion(),
                guardada.getValor(),
                guardada.getCategoria(),
                guardada.getFecha(),
                "Transacción registrada correctamente"
        );
    }
}