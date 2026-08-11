package com.alura.finance_ai.finanzas.service;

import com.alura.finance_ai.finanzas.repository.AnalisisFinancieroRepository;
import com.alura.finance_ai.finanzas.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalisisFinancieroService {

    private final TransaccionRepository transaccionRepository;
    private final AnalisisFinancieroRepository analisisFinancieroRepository;

    public AnalisisFinancieroService(
            TransaccionRepository transaccionRepository,
            AnalisisFinancieroRepository analisisFinancieroRepository) {

        this.transaccionRepository = transaccionRepository;
        this.analisisFinancieroRepository = analisisFinancieroRepository;
    }
}