package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Simulacao;

import org.springframework.stereotype.Component;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.EntregaService;

@Component
public class EntregaSimulada implements EntregaService {
    public String atribuirEntregador(PedidoRegistrado pedido) { return "Entregador simulado 1"; }
}
