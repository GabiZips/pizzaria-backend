package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Simulacao;

import org.springframework.stereotype.Component;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PagamentoService;

@Component
public class PagamentoSimulado implements PagamentoService {
    public boolean confirmar(PedidoRegistrado pedido) { return true; }
}
