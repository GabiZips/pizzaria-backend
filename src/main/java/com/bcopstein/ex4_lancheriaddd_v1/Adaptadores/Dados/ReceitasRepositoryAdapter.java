package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ReceitasRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Receita;

@Repository
@Transactional(readOnly = true)
public class ReceitasRepositoryAdapter implements ReceitasRepository {

    private final ReceitasRepositoryJPA repository;

    public ReceitasRepositoryAdapter(ReceitasRepositoryJPA repository) {
        this.repository = repository;
    }

    @Override
    public Receita recuperaReceita(long id) {
        return repository.findById(id).map(MapeadorJPA::paraDominio).orElse(null);
    }
}
