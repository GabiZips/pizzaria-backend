package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.EstoqueRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

@Service
public class EstoqueService {
    private final EstoqueRepository estoque;
    public EstoqueService(EstoqueRepository estoque) { this.estoque = estoque; }

    public Map<Long, Integer> necessidade(Produto produto, int quantidade) {
        Map<Long, Integer> porcoes = new TreeMap<>();
        for (var ingrediente : produto.getReceita().getIngredientes()) {
            porcoes.merge(ingrediente.getId(), quantidade, Math::addExact);
        }
        return porcoes;
    }

    public List<Long> emFalta(Map<Long, Integer> necessidade) {
        return necessidade.keySet().stream()
                .filter(id -> estoque.porcoes(id) < necessidade.get(id)).toList();
    }

    public boolean disponivel(Produto produto) {
        return !estoque.produtosBloqueados().contains(produto.getId())
                && emFalta(necessidade(produto, 1)).isEmpty();
    }

    public void reservar(Map<Long, Integer> porcoes) {
        porcoes.forEach((id, quantidade) -> estoque.somarPorcoes(id, -quantidade));
    }

    public void devolver(Map<Long, Integer> porcoes) {
        porcoes.forEach(estoque::somarPorcoes);
    }
}
