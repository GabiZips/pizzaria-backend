package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Component;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;

@Component
public class ConsultarPedidoUC {
    private final PedidosRepository pedidos;

    public ConsultarPedidoUC(PedidosRepository pedidos) {
        this.pedidos = pedidos;
    }

    public PedidoRegistrado run(String email, long id) {
        return pedidos.buscar(id)
                .filter(pedido -> pedido.email().equals(email))
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado"));
    }
}
