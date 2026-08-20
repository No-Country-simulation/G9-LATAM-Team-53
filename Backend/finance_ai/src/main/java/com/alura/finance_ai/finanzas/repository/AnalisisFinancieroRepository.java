package com.alura.finance_ai.finanzas.repository;

import com.alura.finance_ai.finanzas.model.AnalisisFinanciero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalisisFinancieroRepository extends JpaRepository<AnalisisFinanciero, Long> {
}