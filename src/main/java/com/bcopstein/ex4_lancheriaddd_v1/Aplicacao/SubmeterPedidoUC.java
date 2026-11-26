package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.*;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.*;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.*;

@Component
public class SubmeterPedidoUC {
    public record Item(long produtoId, int quantidade) {}
    public record Resultado(String status, Long pedidoId, Totais valores, List<Long> itensIndisponiveis) {}

    private final CardapioRepository cardapios;
    private final PreferenciasRepository preferencias;
    private final EstoqueRepository estoque;
    private final EstoqueService disponibilidade;
    private final PedidosRepository pedidos;
    private final PedidosService precos;
    private final Clock relogio;

    public SubmeterPedidoUC(CardapioRepository cardapios, PreferenciasRepository preferencias,
            EstoqueRepository estoque, EstoqueService disponibilidade, PedidosRepository pedidos,
            PedidosService precos, Clock relogio) {
        this.cardapios = cardapios;
        this.preferencias = preferencias;
        this.estoque = estoque;
        this.disponibilidade = disponibilidade;
        this.pedidos = pedidos;
        this.precos = precos;
        this.relogio = relogio;
    }

    @Transactional
    public Resultado run(String email, String endereco, List<Item> entrada) {
        if (endereco == null || endereco.isBlank() || endereco.length() > 255) {
            throw new IllegalArgumentException("Informe um endereço de até 255 caracteres");
        }
        if (entrada == null || entrada.isEmpty() || entrada.size() > 100) {
            throw new IllegalArgumentException("O pedido precisa de 1 a 100 linhas");
        }
        Map<Long, Integer> quantidades = new LinkedHashMap<>();
        for (Item item : entrada) {
            if (item == null || item.produtoId() <= 0 || item.quantidade() < 1 || item.quantidade() > 100) {
                throw new IllegalArgumentException("Produto ou quantidade inválida");
            }
            int total = quantidades.merge(item.produtoId(), item.quantidade(), Integer::sum);
            if (total > 100) throw new IllegalArgumentException("Limite de 100 unidades por produto");
        }

        preferencias.bloquearAlteracoes();
        var cardapio = cardapios.recuperaPorId(preferencias.cardapioCorrente());
        Map<Long, Map<Long, Integer>> porProduto = new LinkedHashMap<>();
        Map<Long, Integer> reserva = new TreeMap<>();
        List<Linha> linhas = new ArrayList<>();
        for (var opcao : quantidades.entrySet()) {
            var produto = cardapio.getProdutos().stream().filter(p -> p.getId() == opcao.getKey())
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Produto fora do cardápio corrente"));
            var porcoes = disponibilidade.necessidade(produto, opcao.getValue());
            porProduto.put(produto.getId(), porcoes);
            porcoes.forEach((id, quantidade) -> reserva.merge(id, quantidade, Integer::sum));
            linhas.add(new Linha(produto.getId(), opcao.getValue(), produto.getPreco()));
        }

        Set<Long> faltantes = new HashSet<>(disponibilidade.emFalta(reserva));
        Set<Long> bloqueados = estoque.produtosBloqueados();
        List<Long> afetados = new ArrayList<>();
        for (var opcao : porProduto.entrySet()) {
            boolean falta = opcao.getValue().keySet().stream().anyMatch(faltantes::contains);
            if (falta) estoque.bloquear(opcao.getKey());
            if (falta || bloqueados.contains(opcao.getKey())) afetados.add(opcao.getKey());
        }
        if (!afetados.isEmpty()) {
            return new Resultado("NEGADO", null, null, List.copyOf(afetados));
        }

        LocalDateTime agora = LocalDateTime.now(relogio);
        long recentes = pedidos.contarPagosDesde(email, agora.minusDays(20), agora);
        var totais = precos.calcular(linhas, recentes);
        disponibilidade.reservar(reserva);
        var pedido = pedidos.salvar(new PedidoRegistrado(null, email, endereco.strip(),
                List.copyOf(linhas), totais, Map.copyOf(reserva), Estado.APROVADO,
                agora, null, null, null,
                List.of(new Evento(Estado.NOVO, agora), new Evento(Estado.APROVADO, agora))));
        return new Resultado("APROVADO", pedido.id(), totais, List.of());
    }
}
