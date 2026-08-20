package com.alura.finance_ai.finanzas.client;

import com.alura.finance_ai.finanzas.client.dto.ClasificacionRequest;
import com.alura.finance_ai.finanzas.client.dto.ClasificacionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class ClasificadorFinancieroClient {

    private final RestClient restClient;

    public ClasificadorFinancieroClient(@Value("${ml.api.url}") String apiUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .build();
    }

    public ClasificacionResponse clasificar(String descripcion, BigDecimal valor, LocalDate fecha) {
        try {
            ClasificacionResponse respuesta = restClient.post()
                    .uri("/predecir")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ClasificacionRequest(descripcion, valor, fecha))
                    .retrieve()
                    .body(ClasificacionResponse.class);

            if (respuesta == null || respuesta.categoriaPredicha() == null || respuesta.categoriaPredicha().isBlank()) {
                throw new ClasificadorNoDisponibleException("El clasificador devolvio una respuesta sin categoria");
            }

            return respuesta;
        } catch (RestClientException e) {
            throw new ClasificadorNoDisponibleException("No se pudo contactar al clasificador financiero", e);
        }
    }
}
