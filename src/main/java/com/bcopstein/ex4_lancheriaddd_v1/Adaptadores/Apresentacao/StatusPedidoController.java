package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.ConsultarPedidoUC;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Estado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Evento;

@RestController
public class StatusPedidoController {
    private final ConsultarPedidoUC consultar;

    public StatusPedidoController(ConsultarPedidoUC consultar) {
        this.consultar = consultar;
    }

    record Progresso(long id, Estado status, String entregador, List<Evento> historico) {}

    @GetMapping("/pedidos/{id}/status")
    Progresso status(@PathVariable long id, Principal pessoa) {
        var pedido = consultar.run(ClienteDaRequisicao.email(pessoa), id);
        return new Progresso(pedido.id(), pedido.estado(), pedido.entregador(), pedido.historico());
    }
}
