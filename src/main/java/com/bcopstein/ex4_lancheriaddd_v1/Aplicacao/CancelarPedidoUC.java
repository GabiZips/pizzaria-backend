package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PreferenciasRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Estado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.EstoqueService;

@Component
public class CancelarPedidoUC {
    private final PreferenciasRepository preferencias;
    private final PedidosRepository pedidos;
    private final ConsultarPedidoUC consultar;
    private final EstoqueService estoque;
    private final Clock relogio;

    public CancelarPedidoUC(PreferenciasRepository preferencias, PedidosRepository pedidos,
            ConsultarPedidoUC consultar, EstoqueService estoque, Clock relogio) {
        this.preferencias = preferencias;
        this.pedidos = pedidos;
        this.consultar = consultar;
        this.estoque = estoque;
        this.relogio = relogio;
    }

    @Transactional
    public void run(String email, long id) {
        preferencias.bloquearAlteracoes();
        var pedido = consultar.run(email, id);
        if (pedido.estado() == Estado.CANCELADO) {
            return;
        }
        if (pedido.estado() != Estado.APROVADO || pedido.pagoEm() != null) {
            throw new IllegalStateException("Apenas pedidos aprovados e não pagos podem ser cancelados");
        }
        cancelar(pedido, LocalDateTime.now(relogio));
    }

    void cancelar(PedidoRegistrado pedido, LocalDateTime agora) {
        try {
            estoque.devolver(pedido.reserva());
        } catch (ArithmeticException erro) {
            throw new IllegalStateException("A devolução ultrapassaria o limite do estoque", erro);
        }
        pedidos.salvar(pedido.avancar(Estado.CANCELADO, agora, null));
    }
}
