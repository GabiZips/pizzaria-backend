package com.bcopstein.ex4_lancheriaddd_v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.SubmeterPedidoUC;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.EstoqueRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Estado;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:status-cancelamento-tests;DB_CLOSE_DELAY=-1",
    "pizzaria.imposto.lei=1000",
    "pizzaria.simulacao.ativa=false"
})
class StatusCancelamentoPedidoTests {
    private static final String CLIENTE = "huguinho.pato@email.com";
    private static final String OUTRO_CLIENTE = "outro@exemplo.com";

    @Autowired WebApplicationContext aplicacao;
    @Autowired SubmeterPedidoUC submeter;
    @Autowired PedidosRepository pedidos;
    @Autowired EstoqueRepository estoque;

    private MockMvc mvc;

    @BeforeEach
    void prepararChamadas() {
        mvc = MockMvcBuilders.webAppContextSetup(aplicacao).build();
    }

    // O dono consulta o histórico e cancela. A repetição preserva um único evento e uma devolução.
    @Test
    void consultaHistoricoECancelaUmaVez() throws Exception {
        int saldoInicial = estoque.porcoes(1L);
        long id = aprovarPedido();
        assertEquals(saldoInicial - 1, estoque.porcoes(1L));

        mvc.perform(get("/pedidos/{id}/status", id).principal(() -> CLIENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.historico[0].estado").value("NOVO"))
                .andExpect(jsonPath("$.historico[1].estado").value("APROVADO"));

        for (int tentativa = 0; tentativa < 2; tentativa++) {
            mvc.perform(post("/pedidos/{id}/cancelamento", id).principal(() -> CLIENTE))
                    .andExpect(status().isNoContent());
            assertEquals(saldoInicial, estoque.porcoes(1L));
        }

        mvc.perform(get("/pedidos/{id}/status", id).principal(() -> CLIENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.historico.length()").value(3))
                .andExpect(jsonPath("$.historico[2].estado").value("CANCELADO"))
                .andExpect(jsonPath("$.historico[2].quando").isString());
    }

    // Um ID ausente ou pertencente a outro cliente gera 404 e preserva o pedido original.
    @Test
    void protegePedidosDeOutroCliente() throws Exception {
        long id = aprovarPedido();
        int saldoReservado = estoque.porcoes(1L);

        mvc.perform(get("/pedidos/{id}/status", id).principal(() -> OUTRO_CLIENTE))
                .andExpect(status().isNotFound());
        mvc.perform(post("/pedidos/{id}/cancelamento", id).principal(() -> OUTRO_CLIENTE))
                .andExpect(status().isNotFound());
        mvc.perform(get("/pedidos/{id}/status", Long.MAX_VALUE).principal(() -> CLIENTE))
                .andExpect(status().isNotFound());
        assertEquals(Estado.APROVADO, pedidos.buscar(id).orElseThrow().estado());
        assertEquals(saldoReservado, estoque.porcoes(1L));
    }

    // Um pedido já pago gera 409 sem devolver a reserva. O estado pago é preparado via JPA.
    @Test
    void recusaCancelarPedidoPago() throws Exception {
        long id = aprovarPedido();
        var pedido = pedidos.buscar(id).orElseThrow();
        pedidos.salvar(pedido.avancar(Estado.PAGO, pedido.aprovadoEm().plusSeconds(1), null));
        int saldoReservado = estoque.porcoes(1L);

        mvc.perform(post("/pedidos/{id}/cancelamento", id).principal(() -> CLIENTE))
                .andExpect(status().isConflict());
        assertEquals(Estado.PAGO, pedidos.buscar(id).orElseThrow().estado());
        assertEquals(saldoReservado, estoque.porcoes(1L));
    }

    // Se devolver uma das porções exceder a capacidade, toda a operação é desfeita.
    @Test
    void conflitoDeEstoquePreservaPedidoEReserva() throws Exception {
        long id = aprovarPedido();
        var pedido = pedidos.buscar(id).orElseThrow();
        int saldoCalabresa = estoque.porcoes(5L);
        int saldoMassa = estoque.porcoes(1L);
        estoque.somarPorcoes(5L, Integer.MAX_VALUE - saldoCalabresa);
        try {
            mvc.perform(post("/pedidos/{id}/cancelamento", id).principal(() -> CLIENTE))
                    .andExpect(status().isConflict());
            var persistido = pedidos.buscar(id).orElseThrow();
            assertEquals(Estado.APROVADO, persistido.estado());
            assertEquals(pedido.reserva(), persistido.reserva());
            assertEquals(2, persistido.historico().size());
            assertEquals(saldoMassa, estoque.porcoes(1L));
            assertEquals(Integer.MAX_VALUE, estoque.porcoes(5L));
        } finally {
            estoque.somarPorcoes(5L, saldoCalabresa - Integer.MAX_VALUE);
        }
    }

    private long aprovarPedido() {
        return submeter.run(CLIENTE, "Rua Exemplo 10",
                List.of(new SubmeterPedidoUC.Item(1L, 1))).pedidoId();
    }
}
