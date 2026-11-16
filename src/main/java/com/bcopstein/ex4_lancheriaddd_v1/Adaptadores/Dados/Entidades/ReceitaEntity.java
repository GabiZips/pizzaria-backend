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
@Table(name = "receitas")
public class ReceitaEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String titulo;

    // A tabela intermediaria associa as receitas aos seus ingredientes.
    @ManyToMany
    @JoinTable(name = "receita_ingrediente",
            joinColumns = @JoinColumn(name = "receita_id"),
            inverseJoinColumns = @JoinColumn(name = "ingrediente_id"))
    @OrderBy("id")
    private List<IngredienteEntity> ingredientes = new ArrayList<>();

    protected ReceitaEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<IngredienteEntity> getIngredientes() {
        return ingredientes;
    }
}
