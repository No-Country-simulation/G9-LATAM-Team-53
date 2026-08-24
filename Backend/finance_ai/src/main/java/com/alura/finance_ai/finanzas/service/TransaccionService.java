package com.alura.finance_ai.finanzas.service;

import com.alura.finance_ai.auth.model.User;
import com.alura.finance_ai.auth.repository.UserRepository;
import com.alura.finance_ai.finanzas.dto.ActualizarCategoriaTransaccionRequest;
import com.alura.finance_ai.finanzas.client.ClasificadorFinancieroClient;
import com.alura.finance_ai.finanzas.client.dto.ClasificacionResponse;
import com.alura.finance_ai.finanzas.dto.TransaccionRequest;
import com.alura.finance_ai.finanzas.dto.TransaccionResponse;
import com.alura.finance_ai.finanzas.model.Categoria;
import com.alura.finance_ai.finanzas.model.Transaccion;
import com.alura.finance_ai.finanzas.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;

@Service
public class TransaccionService {

    private static final String CATEGORIA_POR_DEFECTO = "otros";

    private final TransaccionRepository transaccionRepository;
    private final UserRepository userRepository;
    private final CategoriaService categoriaService;
    private final ClasificadorFinancieroClient clasificadorFinancieroClient;

    public TransaccionService(TransaccionRepository transaccionRepository,
                              UserRepository userRepository,
                              CategoriaService categoriaService,
                              ClasificadorFinancieroClient clasificadorFinancieroClient) {
        this.transaccionRepository = transaccionRepository;
        this.userRepository = userRepository;
        this.categoriaService = categoriaService;
        this.clasificadorFinancieroClient = clasificadorFinancieroClient;
    }

    @Transactional(readOnly = true)
    public Page<TransaccionResponse> listarTransacciones(String userEmail, int pagina, int tamanio) {
        User usuario = buscarUsuarioPorEmail(userEmail);
        PageRequest paginacion = PageRequest.of(pagina, tamanio, Sort.by("fecha").descending().and(Sort.by("idTransaccion").descending()));
        return transaccionRepository.findByUsuarioAndActivaTrue(usuario, paginacion)
                .map(this::mapearRespuesta);
    }


    @Transactional
    public TransaccionResponse registrarTransaccion(TransaccionRequest request, String userEmail) {
        User usuario = buscarUsuarioPorEmail(userEmail);
        LocalDate fecha = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        ClasificacionResponse clasificacion = clasificadorFinancieroClient.clasificar(
                request.descripcion(), request.valor(), fecha);
        Categoria categoria = buscarCategoriaPorNombre(clasificacion.categoriaPredicha());
        Transaccion transaccion = mapearEntidad(request, usuario, categoria, fecha);
        Transaccion guardada = transaccionRepository.save(transaccion);
        return mapearRespuesta(guardada);
    }

    @Transactional
    public TransaccionResponse actualizarCategoriaTransaccion(Long idTransaccion,
                                                             ActualizarCategoriaTransaccionRequest request,
                                                             String userEmail) {
        User usuario = buscarUsuarioPorEmail(userEmail);
        Transaccion transaccion = transaccionRepository.findByIdTransaccionAndUsuarioAndActivaTrue(idTransaccion, usuario)
                .orElseThrow(() -> new IllegalArgumentException("La transaccion no existe o no pertenece al usuario autenticado"));

        Categoria categoria = buscarCategoriaPorId(request.categoriaId());
        transaccion.setCategoria(categoria);

        Transaccion actualizada = transaccionRepository.save(transaccion);
        return mapearRespuesta(actualizada);
    }

    private User buscarUsuarioPorEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Categoria buscarCategoriaPorNombre(String categoriaPredicha) {
        String nombreNormalizado = categoriaPredicha.trim().toLowerCase(Locale.ROOT);
        return categoriaService.buscarPorNombre(nombreNormalizado)
                .orElseGet(() -> categoriaService.buscarPorNombre(CATEGORIA_POR_DEFECTO)
                        .orElseThrow(() -> new IllegalStateException(
                                "No existe la categoria por defecto: " + CATEGORIA_POR_DEFECTO)));
    }

    private Categoria buscarCategoriaPorId(Long categoriaId) {
        return categoriaService.buscarPorId(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("La categoria no existe"));
    }

    private Transaccion mapearEntidad(TransaccionRequest request,
                                      User usuario,
                                      Categoria categoria,
                                      LocalDate fecha) {
        return Transaccion.builder()
                .descripcion(request.descripcion())
                .valor(request.valor())
                .categoria(categoria)
                .fecha(fecha)
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
