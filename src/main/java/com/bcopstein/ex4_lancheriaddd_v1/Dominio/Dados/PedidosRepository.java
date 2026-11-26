package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Estado;

public interface PedidosRepository {
    PedidoRegistrado salvar(PedidoRegistrado pedido);
    Optional<PedidoRegistrado> buscar(long id);
    long contarPagosDesde(String email, LocalDateTime inicio, LocalDateTime fim);
    List<PedidoRegistrado> porEstado(Estado estado);
    List<PedidoRegistrado> entregues(String email, LocalDateTime de, LocalDateTime ateExclusivo);
}
