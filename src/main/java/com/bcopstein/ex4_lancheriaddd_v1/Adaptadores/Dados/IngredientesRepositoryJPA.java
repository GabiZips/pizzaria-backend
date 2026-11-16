package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados.Entidades.IngredienteEntity;

public interface IngredientesRepositoryJPA
        extends JpaRepository<IngredienteEntity, Long> {

    @Query("select i from ReceitaEntity r join r.ingredientes i "
            + "where r.id = :receitaId order by i.id")
    List<IngredienteEntity> buscaPorReceita(@Param("receitaId") long receitaId);
}
