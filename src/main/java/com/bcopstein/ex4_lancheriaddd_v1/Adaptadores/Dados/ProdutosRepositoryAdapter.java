package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

@Repository
@Transactional(readOnly = true)
public class ProdutosRepositoryAdapter implements ProdutosRepository {

    private final ProdutosRepositoryJPA repository;

    public ProdutosRepositoryAdapter(ProdutosRepositoryJPA repository) {
        this.repository = repository;
    }

    @Override
    public Produto recuperaProdutoPorid(long id) {
        return repository.findById(id).map(MapeadorJPA::paraDominio).orElse(null);
    }

    @Override
    public List<Produto> recuperaProdutosCardapio(long id) {
        return repository.buscaPorCardapio(id).stream()
                .map(MapeadorJPA::paraDominio).toList();
    }
}
