package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados.Entidades;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "cardapios")
public class CardapioEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @ManyToMany
    @JoinTable(name = "cardapio_produto",
            joinColumns = @JoinColumn(name = "cardapio_id"),
            inverseJoinColumns = @JoinColumn(name = "produto_id"))
    @OrderBy("id")
    private List<ProdutoEntity> produtos = new ArrayList<>();

    protected CardapioEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<ProdutoEntity> getProdutos() {
        return produtos;
    }
}
