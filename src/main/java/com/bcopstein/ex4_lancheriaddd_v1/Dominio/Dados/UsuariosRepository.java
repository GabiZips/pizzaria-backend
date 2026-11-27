package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import java.util.Optional;

public interface UsuariosRepository {
    record Usuario(String cpf, String nome, String telefone, String endereco,
            String email, String senhaHash, String perfil) {}
    Optional<Usuario> porEmail(String email);
    boolean existeCpf(String cpf);
    void cadastrar(Usuario usuario);
}
