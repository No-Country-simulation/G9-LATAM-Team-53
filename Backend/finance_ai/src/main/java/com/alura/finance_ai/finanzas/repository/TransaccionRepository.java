package com.alura.finance_ai.finanzas.repository;

import com.alura.finance_ai.auth.model.User;
import com.alura.finance_ai.finanzas.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByUsuarioAndActivaTrue(User usuario);

}