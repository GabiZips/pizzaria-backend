package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.CardapioRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.CabecalhoCardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

@Repository
@Transactional(readOnly = true)
public class CardapioRepositoryAdapter implements CardapioRepository {

    private final CardapioRepositoryJPA repository;
    private final ProdutosRepository produtosRepository;

    public CardapioRepositoryAdapter(CardapioRepositoryJPA repository,
            ProdutosRepository produtosRepository) {
        this.repository = repository;
        this.produtosRepository = produtosRepository;
    }

    @Override
    public Cardapio recuperaPorId(long id) {
        return repository.findById(id).map(MapeadorJPA::paraDominio).orElse(null);
    }

    @Override
    public List<CabecalhoCardapio> cardapiosDisponiveis() {
        return repository.findAll(Sort.by("id")).stream()
                .map(MapeadorJPA::paraCabecalho).toList();
    }

    @Override
    public List<Produto> indicacoesDoChef() {
        // Mantem a indicacao fixa usada no estudo de caso.
        return List.of(produtosRepository.recuperaProdutoPorid(2L));
    }
}
