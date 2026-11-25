package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.RecuperarCardapioUC;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PreferenciasRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.EstoqueService;

@Component
public class CarregarCardapioCorrenteUC {
    public record Item(long id, String descricao, int preco, boolean indicacao, boolean disponivel) {}
    public record Menu(String titulo, List<Item> itens) {}

    private final RecuperarCardapioUC recuperar;
    private final PreferenciasRepository preferencias;
    private final EstoqueService estoque;

    public CarregarCardapioCorrenteUC(RecuperarCardapioUC recuperar, PreferenciasRepository preferencias,
            EstoqueService estoque) {
        this.recuperar = recuperar;
        this.preferencias = preferencias;
        this.estoque = estoque;
    }

    @Transactional(readOnly = true)
    public Menu run() {
        var resposta = recuperar.run(preferencias.cardapioCorrente());
        Set<Long> sugestoes = resposta.getSugestoesDoChef().stream()
                .map(p -> p.getId()).collect(Collectors.toSet());
        var cardapio = resposta.getCardapio();
        var itens = cardapio.getProdutos().stream()
                .map(p -> new Item(p.getId(), p.getDescricao(), p.getPreco(),
                        sugestoes.contains(p.getId()), estoque.disponivel(p))).toList();
        return new Menu(cardapio.getCabecalhoCardapio().titulo(), itens);
    }
}
