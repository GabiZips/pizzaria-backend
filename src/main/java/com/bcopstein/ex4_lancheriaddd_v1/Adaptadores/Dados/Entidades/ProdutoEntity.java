package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados.Entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "produtos")
public class ProdutoEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String descricao;

    // O schema existente armazena o preco em centavos como BIGINT.
    private Long preco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "produto_receita",
            joinColumns = @JoinColumn(name = "produto_id"),
            inverseJoinColumns = @JoinColumn(name = "receita_id"))
    private ReceitaEntity receita;

    protected ProdutoEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public Long getPreco() {
        return preco;
    }

    public ReceitaEntity getReceita() {
        return receita;
    }
}
