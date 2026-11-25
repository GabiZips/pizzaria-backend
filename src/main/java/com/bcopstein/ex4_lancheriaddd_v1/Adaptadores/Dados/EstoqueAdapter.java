package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.EstoqueRepository;
import java.util.HashSet;
import java.util.Set;

@Repository
@Transactional(readOnly = true)
public class EstoqueAdapter implements EstoqueRepository {
    private final EntityManager banco;
    public EstoqueAdapter(EntityManager banco) { this.banco = banco; }

    private EstoqueEntity buscar(long ingredienteId) {
        return banco.createQuery("from EstoqueEntity e where e.ingredienteId = :id", EstoqueEntity.class)
                .setParameter("id", ingredienteId).getResultStream().findFirst().orElse(null);
    }

    public int porcoes(long ingredienteId) {
        var item = buscar(ingredienteId);
        return item == null ? 0 : item.quantidade;
    }

    @Transactional
    public void somarPorcoes(long ingredienteId, int quantidade) {
        var item = buscar(ingredienteId);
        if (item == null) throw new java.util.NoSuchElementException("Ingrediente sem registro de estoque");
        int novo = Math.addExact(item.quantidade, quantidade);
        if (novo < 0) throw new IllegalStateException("Estoque insuficiente");
        item.quantidade = novo;
    }

    public Set<Long> produtosBloqueados() {
        return new HashSet<>(banco.createQuery("select b.produtoId from ProdutoBloqueadoEntity b", Long.class)
                .getResultList());
    }

    @Transactional
    public void bloquear(long produtoId) {
        if (banco.find(ProdutoBloqueadoEntity.class, produtoId) == null) {
            banco.persist(new ProdutoBloqueadoEntity(produtoId));
        }
    }

    @Transactional
    public void desbloquear(long produtoId) {
        var linha = banco.find(ProdutoBloqueadoEntity.class, produtoId);
        if (linha != null) banco.remove(linha);
    }
}
