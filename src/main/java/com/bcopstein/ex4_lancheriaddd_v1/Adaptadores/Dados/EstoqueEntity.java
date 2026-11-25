package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import jakarta.persistence.*;

@Entity
@Table(name = "itensestoque")
public class EstoqueEntity {
    @Id Long id;
    long ingredienteId;
    int quantidade;

    protected EstoqueEntity() {}
}
