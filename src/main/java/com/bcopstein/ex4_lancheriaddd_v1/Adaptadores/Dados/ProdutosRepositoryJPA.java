package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados.Entidades.ProdutoEntity;

public interface ProdutosRepositoryJPA extends JpaRepository<ProdutoEntity, Long> {

    @Query("select p from CardapioEntity c join c.produtos p "
            + "where c.id = :cardapioId order by p.id")
    List<ProdutoEntity> buscaPorCardapio(@Param("cardapioId") long cardapioId);
}
