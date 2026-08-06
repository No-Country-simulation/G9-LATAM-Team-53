package com.alura.finance_ai.finanzas.service;

import com.alura.finance_ai.auth.model.User;
import com.alura.finance_ai.auth.repository.UserRepository;
import com.alura.finance_ai.finanzas.dto.TransaccionRequest;
import com.alura.finance_ai.finanzas.dto.TransaccionResponse;
import com.alura.finance_ai.finanzas.model.Categoria;
import com.alura.finance_ai.finanzas.model.Transaccion;
import com.alura.finance_ai.finanzas.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final UserRepository userRepository;
    private final CategoriaService categoriaService;

    public TransaccionService(TransaccionRepository transaccionRepository,
                              UserRepository userRepository,
                              CategoriaService categoriaService) {
        this.transaccionRepository = transaccionRepository;
        this.userRepository = userRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional
    public TransaccionResponse registrarTransaccion(TransaccionRequest request, String userEmail) {
        User usuario = buscarUsuarioPorEmail(userEmail);
        Categoria categoria = buscarCategoriaPorId(request.categoriaId());
        Transaccion transaccion = mapearEntidad(request, usuario, categoria);
        Transaccion guardada = transaccionRepository.save(transaccion);
        return mapearRespuesta(guardada);
    }

    private User buscarUsuarioPorEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Categoria buscarCategoriaPorId(Long categoriaId) {
        return categoriaService.buscarPorId(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
    }

    private Transaccion mapearEntidad(TransaccionRequest request, User usuario, Categoria categoria) {
        return Transaccion.builder()
                .descripcion(request.descripcion())
                .valor(request.valor())
                .categoria(categoria)
                .fecha(request.fecha())
                .usuario(usuario)
                .activa(true)
                .build();
    }

    private TransaccionResponse mapearRespuesta(Transaccion guardada) {
        return new TransaccionResponse(
                guardada.getIdTransaccion(),
                guardada.getDescripcion(),
                guardada.getValor(),
                guardada.getCategoria().getId(),
                guardada.getCategoria().getNombre(),
                guardada.getFecha(),
                "Transaccion registrada correctamente"
        );
    }
}
