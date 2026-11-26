package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.*;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.EstoqueService;

@Component
public class ReporEstoqueUC {
    private final PreferenciasRepository preferencias;
    private final EstoqueRepository estoque;
    private final ProdutosRepository produtos;
    private final EstoqueService disponibilidade;

    public ReporEstoqueUC(PreferenciasRepository preferencias, EstoqueRepository estoque,
            ProdutosRepository produtos, EstoqueService disponibilidade) {
        this.preferencias = preferencias;
        this.estoque = estoque;
        this.produtos = produtos;
        this.disponibilidade = disponibilidade;
    }

    @Transactional
    public void run(long ingredienteId, int quantidade) {
        if (ingredienteId < 1 || quantidade < 1 || quantidade > 100_000) {
            throw new IllegalArgumentException("Reposição inválida");
        }
        preferencias.bloquearAlteracoes();
        if (estoque.porcoes(ingredienteId) > Integer.MAX_VALUE - quantidade) {
            throw new IllegalArgumentException("Limite de estoque excedido");
        }
        estoque.somarPorcoes(ingredienteId, quantidade);
        for (long produtoId : estoque.produtosBloqueados()) {
            var produto = produtos.recuperaProdutoPorid(produtoId);
            var porcoes = disponibilidade.necessidade(produto, 1);
            if (porcoes.containsKey(ingredienteId) && disponibilidade.emFalta(porcoes).isEmpty()) {
                estoque.desbloquear(produtoId);
            }
        }
    }
}
