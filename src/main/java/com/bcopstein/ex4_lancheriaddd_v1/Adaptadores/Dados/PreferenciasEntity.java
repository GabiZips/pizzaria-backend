package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import jakarta.persistence.*;

@Entity
@Table(name = "preferencias_pizzaria")
public class PreferenciasEntity {
    @Id Integer id;
    long cardapioId;
    String descontoCodigo;

    protected PreferenciasEntity() {}
}
