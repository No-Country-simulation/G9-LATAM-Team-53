package com.alura.finance_ai.finanzas.repository;

import com.alura.finance_ai.finanzas.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
}