package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Component;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.*;

import java.time.LocalDate;
import java.util.List;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;

@Component
public class ListarEntreguesUC {
    private final PedidosRepository pedidos;
    public ListarEntreguesUC(PedidosRepository pedidos) { this.pedidos = pedidos; }

    public List<PedidoRegistrado> run(String email, LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio) || fim.equals(LocalDate.MAX)) {
            throw new IllegalArgumentException("Intervalo inválido");
        }
        return pedidos.entregues(email, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
    }
}
