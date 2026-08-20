package com.alura.finance_ai.finanzas.repository;

import com.alura.finance_ai.auth.model.User;
import com.alura.finance_ai.finanzas.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByUsuarioAndActivaTrue(User usuario);

    @Query("""
            select t.categoria.nombre, sum(t.valor)
            from Transaccion t
            where t.usuario = :usuario
              and t.activa = true
              and t.fecha >= :inicio
              and t.fecha < :fin
            group by t.categoria.nombre
            """)
    List<Object[]> totalGastosPorCategoriaDelPeriodo(@Param("usuario") User usuario,
                                                     @Param("inicio") LocalDate inicio,
                                                     @Param("fin") LocalDate fin);

}
