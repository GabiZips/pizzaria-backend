package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados.Entidades.CardapioEntity;
import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados.Entidades.IngredienteEntity;
import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados.Entidades.ProdutoEntity;
import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados.Entidades.ReceitaEntity;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.CabecalhoCardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Ingrediente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Receita;

// Converte os objetos de persistencia para os tipos definidos pelo dominio.
final class MapeadorJPA {

    private MapeadorJPA() {
    }

    static Ingrediente paraDominio(IngredienteEntity entity) {
        return new Ingrediente(entity.getId(), entity.getDescricao());
    }

    static Receita paraDominio(ReceitaEntity entity) {
        return new Receita(entity.getId(), entity.getTitulo(),
                entity.getIngredientes().stream().map(MapeadorJPA::paraDominio).toList());
    }

    static Produto paraDominio(ProdutoEntity entity) {
        return new Produto(entity.getId(), entity.getDescricao(),
                paraDominio(entity.getReceita()), Math.toIntExact(entity.getPreco()));
    }

    static CabecalhoCardapio paraCabecalho(CardapioEntity entity) {
        return new CabecalhoCardapio(entity.getId(), entity.getTitulo());
    }

    static Cardapio paraDominio(CardapioEntity entity) {
        return new Cardapio(paraCabecalho(entity),
                entity.getProdutos().stream().map(MapeadorJPA::paraDominio).toList());
    }
}
