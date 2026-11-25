package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import jakarta.persistence.*;

@Entity
@Table(name = "produto_indisponivel")
public class ProdutoBloqueadoEntity {
    @Id Long produtoId;

    protected ProdutoBloqueadoEntity() {}
    ProdutoBloqueadoEntity(long produtoId) { this.produtoId = produtoId; }
}
