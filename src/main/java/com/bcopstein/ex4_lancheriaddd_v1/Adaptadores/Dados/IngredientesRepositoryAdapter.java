package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.IngredientesRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Ingrediente;

@Repository
@Transactional(readOnly = true)
public class IngredientesRepositoryAdapter implements IngredientesRepository {

    private final IngredientesRepositoryJPA repository;

    public IngredientesRepositoryAdapter(IngredientesRepositoryJPA repository) {
        this.repository = repository;
    }

    @Override
    public List<Ingrediente> recuperaTodos() {
        return repository.findAll(Sort.by("id")).stream()
                .map(MapeadorJPA::paraDominio).toList();
    }

    @Override
    public List<Ingrediente> recuperaIngredientesReceita(long id) {
        return repository.buscaPorReceita(id).stream()
                .map(MapeadorJPA::paraDominio).toList();
    }
}
