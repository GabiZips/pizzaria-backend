package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;

public interface EntregaService {
    String atribuirEntregador(PedidoRegistrado pedido);
}
