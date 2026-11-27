package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Component;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.*;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Estado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PagamentoService;

@Component
public class PagarPedidoUC {
    public record Confirmacao(long pedidoId, Estado status, String mensagem) {}

    private final PreferenciasRepository preferencias;
    private final PedidosRepository pedidos;
    private final ConsultarPedidoUC consultar;
    private final PagamentoService pagamento;
    private final Clock relogio;

    public PagarPedidoUC(PreferenciasRepository preferencias, PedidosRepository pedidos,
            ConsultarPedidoUC consultar, PagamentoService pagamento, Clock relogio) {
        this.preferencias = preferencias;
        this.pedidos = pedidos;
        this.consultar = consultar;
        this.pagamento = pagamento;
        this.relogio = relogio;
    }

    @Transactional
    public Confirmacao run(String email, long id) {
        preferencias.bloquearAlteracoes();
        var pedido = consultar.run(email, id);
        if (pedido.pagoEm() != null) {
            return new Confirmacao(id, pedido.estado(), "Pagamento já registrado para o pedido " + id);
        }
        LocalDateTime agora = LocalDateTime.now(relogio);
        if (pedido.estado() != Estado.APROVADO || CancelarPedidoUC.expirado(pedido, agora)) {
            throw new IllegalStateException("Pedido sem aprovação ativa");
        }
        if (!pagamento.confirmar(pedido)) throw new IllegalStateException("Pagamento recusado");
        pedidos.salvar(pedido.avancar(Estado.PAGO, agora, null));
        return new Confirmacao(id, Estado.PAGO, "Pagamento confirmado para o pedido " + id);
    }
}
