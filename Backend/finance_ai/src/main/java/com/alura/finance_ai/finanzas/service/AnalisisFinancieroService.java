package com.alura.finance_ai.finanzas.service;

import com.alura.finance_ai.auth.model.User;
import com.alura.finance_ai.auth.repository.UserRepository;
import com.alura.finance_ai.finanzas.dto.AnalisisFinancieroResponse;
import com.alura.finance_ai.finanzas.dto.IngresoMensualResponse;
import com.alura.finance_ai.finanzas.model.PerfilFinanciero;
import com.alura.finance_ai.finanzas.dto.EstadoIngresoMensualResponse;
import com.alura.finance_ai.finanzas.repository.AnalisisFinancieroRepository;
import com.alura.finance_ai.finanzas.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class AnalisisFinancieroService {

    private final UserRepository userRepository;
    private final AnalisisFinancieroRepository analisisFinancieroRepository;
    private final TransaccionRepository transaccionRepository;
    private final CategoriaService categoriaService;

    public AnalisisFinancieroService(UserRepository userRepository,
                                     AnalisisFinancieroRepository analisisFinancieroRepository,
                                     TransaccionRepository transaccionRepository,
                                     CategoriaService categoriaService) {
        this.userRepository = userRepository;
        this.analisisFinancieroRepository = analisisFinancieroRepository;
        this.transaccionRepository = transaccionRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public AnalisisFinancieroResponse obtenerAnalisisFinanciero(String userEmail) {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate inicioMesSiguiente = inicioMes.plusMonths(1);

        Map<String, BigDecimal> gastosPorCategoria = new LinkedHashMap<>();
        categoriaService.listarCategorias().stream()
                .sorted((primera, segunda) -> primera.getNombre().compareToIgnoreCase(segunda.getNombre()))
                .forEach(categoria -> gastosPorCategoria.put(categoria.getNombre(), BigDecimal.ZERO));

        transaccionRepository.totalGastosPorCategoriaDelPeriodo(usuario, inicioMes, inicioMesSiguiente)
                .forEach(fila -> gastosPorCategoria.put((String) fila[0], (BigDecimal) fila[1]));

        BigDecimal ingresoMensual = usuario.getIngresoMensual();
        if (ingresoMensual == null || ingresoMensual.signum() <= 0) {
            throw new IllegalArgumentException("El usuario debe registrar un ingreso mensual mayor a cero");
        }

        Map<String, BigDecimal> porcentajePorCategoria = new LinkedHashMap<>();
        gastosPorCategoria.forEach((categoria, gasto) -> porcentajePorCategoria.put(
                categoria,
                gasto.multiply(BigDecimal.valueOf(100))
                        .divide(ingresoMensual, 2, RoundingMode.HALF_UP)
        ));

        BigDecimal totalGastado = gastosPorCategoria.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal montoRestante = ingresoMensual.subtract(totalGastado);

        String nombreYApellido = usuario.getNombre() + " " + usuario.getApellido();
        String mesYFecha = hoy.format(DateTimeFormatter.ofPattern("MM/uuuu"));

        List<String> recomendaciones = evaluarSugerencias(porcentajePorCategoria);
        PerfilFinanciero perfil = determinarPerfil(totalGastado, ingresoMensual, recomendaciones);

        return new AnalisisFinancieroResponse(
                nombreYApellido,
                mesYFecha,
                gastosPorCategoria,
                porcentajePorCategoria,
                montoRestante,
                perfil,
                recomendaciones
        );
    }

    @Transactional(readOnly = true)
    public EstadoIngresoMensualResponse obtenerEstadoIngresoMensual(String userEmail) {
        User usuario = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        BigDecimal ingresoMensual = usuario.getIngresoMensual();
        boolean registrado = ingresoMensual != null && ingresoMensual.signum() > 0;
        return new EstadoIngresoMensualResponse(registrado, registrado ? ingresoMensual : null);
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

    private static final Map<String, BigDecimal> TOPES_RECOMENDADOS = Map.ofEntries(
            Map.entry("Vivienda", new BigDecimal("30")),
            Map.entry("Alimentación", new BigDecimal("12")),
            Map.entry("Transporte", new BigDecimal("10")),
            Map.entry("Salud", new BigDecimal("5")),
            Map.entry("Educación", new BigDecimal("5")),
            Map.entry("Servicios", new BigDecimal("8")),
            Map.entry("Streaming", new BigDecimal("3")),
            Map.entry("Ocio", new BigDecimal("7")),
            Map.entry("Otros", new BigDecimal("5"))
    );

    private List<String> evaluarSugerencias(Map<String, BigDecimal> porcentajePorCategoria) {
        List<String> sugerencias = new ArrayList<>();

        porcentajePorCategoria.forEach((categoria, porcentajeGastado) -> {
            BigDecimal tope = TOPES_RECOMENDADOS.getOrDefault(categoria, new BigDecimal("5"));
            if (porcentajeGastado.compareTo(tope) > 0) {
                BigDecimal exceso = porcentajeGastado.subtract(tope);
                sugerencias.add(String.format(
                        "Atención en %s: estás gastando un %.2f%% de tu ingreso (tope recomendado: %.0f%%). Exceso: %.2f%%. Considerá ajustar tus gastos en esta categoría.",
                        categoria, porcentajeGastado, tope, exceso
                ));
            }
        });

        if (sugerencias.isEmpty()) {
            sugerencias.add("¡Felicitaciones! Tus gastos por categoría se encuentran dentro de los límites recomendados.");
        }

        return sugerencias;
    }

    private PerfilFinanciero determinarPerfil(BigDecimal totalGastado, BigDecimal ingresoMensual, List<String> recomendaciones) {
        BigDecimal porcentajeTotalGastado = totalGastado.multiply(new BigDecimal("100"))
                .divide(ingresoMensual, 2, RoundingMode.HALF_UP);

        if (porcentajeTotalGastado.compareTo(new BigDecimal("100")) > 0 || recomendaciones.size() >= 4) {
            return PerfilFinanciero.EN_RIESGO;
        } else if (porcentajeTotalGastado.compareTo(new BigDecimal("80")) > 0 || recomendaciones.size() >= 2) {
            return PerfilFinanciero.EN_OBSERVACION;
        } else {
            return PerfilFinanciero.SALUDABLE;
        }
    }
}
