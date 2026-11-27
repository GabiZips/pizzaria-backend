package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.UsuariosRepository;

@Repository
@Transactional(readOnly = true)
public class UsuariosAdapter implements UsuariosRepository {
    private final EntityManager banco;
    public UsuariosAdapter(EntityManager banco) { this.banco = banco; }

    public Optional<Usuario> porEmail(String email) {
        return banco.createQuery("from UsuarioEntity u where u.email = :email", UsuarioEntity.class)
                .setParameter("email", email).getResultStream().findFirst()
                .map(u -> new Usuario(u.cpf, u.nome, u.celular, u.endereco, u.email, u.senhaHash, u.perfil));
    }

    public boolean existeCpf(String cpf) { return banco.find(UsuarioEntity.class, cpf) != null; }

    @Transactional
    public void cadastrar(Usuario usuario) {
        var linha = new UsuarioEntity();
        linha.cpf = usuario.cpf();
        linha.nome = usuario.nome();
        linha.celular = usuario.telefone();
        linha.endereco = usuario.endereco();
        linha.email = usuario.email();
        linha.senhaHash = usuario.senhaHash();
        linha.perfil = usuario.perfil();
        banco.persist(linha);
    }
}
