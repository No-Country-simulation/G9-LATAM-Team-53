package com.alura.finance_ai.auth.repository;

import com.alura.finance_ai.auth.model.TokenInvalido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenInvalidoRepository extends JpaRepository<TokenInvalido, Long> {

    boolean existsByToken(String token);

}
