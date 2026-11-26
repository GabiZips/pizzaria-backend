package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record PedidoRegistrado(Long id, String email, String endereco,
        List<Linha> itens, Totais totais, Map<Long, Integer> reserva,
        Estado estado, LocalDateTime aprovadoEm, LocalDateTime pagoEm,
        LocalDateTime entregueEm, String entregador, List<Evento> historico) {

    public enum Estado { NOVO, APROVADO, CANCELADO, PAGO, AGUARDANDO, PREPARACAO, PRONTO, TRANSPORTE, ENTREGUE }
    public record Linha(long produtoId, int quantidade, long precoUnitario) {}
    public record Totais(long subtotal, long desconto, long imposto, long total) {}
    public record Evento(Estado estado, LocalDateTime quando) {}

    public PedidoRegistrado avancar(Estado proximo, LocalDateTime agora, String novoEntregador) {
        List<Evento> eventos = new ArrayList<>(historico);
        eventos.add(new Evento(proximo, agora));
        return new PedidoRegistrado(id, email, endereco, itens, totais, reserva, proximo,
                aprovadoEm, proximo == Estado.PAGO ? agora : pagoEm,
                proximo == Estado.ENTREGUE ? agora : entregueEm, novoEntregador, List.copyOf(eventos));
    }
}
