package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class UsuarioEntity {
    @Id String cpf;
    String nome;
    String celular;
    String endereco;
    String email;
    String senhaHash;
    String perfil;

    protected UsuarioEntity() {}
}
