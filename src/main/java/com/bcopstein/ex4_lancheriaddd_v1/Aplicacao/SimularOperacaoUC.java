package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Component;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.*;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Estado;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ICozinhaService;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.EntregaService;

@Component
public class SimularOperacaoUC {
    private static final Logger LOG = LoggerFactory.getLogger(SimularOperacaoUC.class);

    private final PreferenciasRepository preferencias;
    private final PedidosRepository pedidos;
    private final CancelarPedidoUC cancelar;
    private final ICozinhaService cozinha;
    private final EntregaService entrega;
    private final Clock relogio;

    public SimularOperacaoUC(PreferenciasRepository preferencias, PedidosRepository pedidos,
            CancelarPedidoUC cancelar, ICozinhaService cozinha, EntregaService entrega, Clock relogio) {
        this.preferencias = preferencias;
        this.pedidos = pedidos;
        this.cancelar = cancelar;
        this.cozinha = cozinha;
        this.entrega = entrega;
        this.relogio = relogio;
    }

    @Transactional
    public void executarCiclo() {
        preferencias.bloquearAlteracoes();
        LocalDateTime agora = LocalDateTime.now(relogio);

        List<PedidoRegistrado> emTransporte = pedidos.porEstado(Estado.TRANSPORTE);
        if (emTransporte.isEmpty()) {
            pedidos.porEstado(Estado.PRONTO).stream().findFirst().ifPresent(pedido ->
                    pedidos.salvar(pedido.avancar(Estado.TRANSPORTE, agora, entrega.atribuirEntregador(pedido))));
        } else {
            var pedido = emTransporte.getFirst();
            pedidos.salvar(pedido.avancar(Estado.ENTREGUE, agora, pedido.entregador()));
        }

        List<PedidoRegistrado> preparando = pedidos.porEstado(Estado.PREPARACAO);
        if (preparando.isEmpty()) {
            pedidos.porEstado(Estado.AGUARDANDO).stream().findFirst().ifPresent(p -> avancarCozinha(p, agora));
        } else {
            avancarCozinha(preparando.getFirst(), agora);
        }
        pedidos.porEstado(Estado.PAGO).forEach(p -> avancarCozinha(p, agora));
    }

    public void expirarReservas() {
        var limite = CancelarPedidoUC.limiteAprovacao(LocalDateTime.now(relogio));
        for (long id : pedidos.idsDeAprovadosAte(limite)) {
            try {
                // Cada devolução tem sua própria transação. A fila de entregas já foi gravada.
                cancelar.cancelarSeExpirado(id);
            } catch (IllegalStateException conflito) {
                LOG.warn("Pedido {} aguardando liberação da reserva: {}", id, conflito.getMessage());
            }
        }
    }

    private void avancarCozinha(PedidoRegistrado pedido, LocalDateTime agora) {
        pedidos.salvar(pedido.avancar(cozinha.proximoEstado(pedido.estado()), agora, null));
    }
}
