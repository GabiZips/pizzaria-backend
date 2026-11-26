package com.bcopstein.ex4_lancheriaddd_v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.EstoqueRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Estado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.DescontosService;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:submissao-tests;DB_CLOSE_DELAY=-1",
    "pizzaria.imposto.lei=1000",
    "pizzaria.simulacao.ativa=false"
})
class SubmeterPedidoIntegracaoTests {
    private static final String CLIENTE = "huguinho.pato@email.com";

    @Autowired WebApplicationContext aplicacao;
    @Autowired EstoqueRepository estoque;
    @Autowired PedidosRepository pedidos;
    @Autowired DescontosService descontos;

    /*
     * Cenário: entrada inválida, aprovação com promoção, falta de ingrediente compartilhado e reposição.
     * Esperado: 400 sem escrita, 201 com valores e reserva persistidos, 409 sem baixa parcial,
     * bloqueio visível no cardápio e liberação após reposição.
     */
    @Test
    void aprovaReservaNegaSemBaixaParcialEReabilitaAposReposicao() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(aplicacao).build();
        descontos.definir("PromocaoVerao");

        mvc.perform(post("/pedidos").principal(() -> CLIENTE).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"endereco":"Rua Exemplo 10","itens":[{"produtoId":1,"quantidade":0}]}
                        """))
                .andExpect(status().isBadRequest());
        assertEquals(0, pedidos.porEstado(Estado.APROVADO).size());
        assertEquals(30, estoque.porcoes(1L));

        mvc.perform(post("/pedidos").principal(() -> CLIENTE).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"endereco":"Rua Exemplo 10","itens":[{"produtoId":1,"quantidade":1}]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.pedidoId").isNumber())
                .andExpect(jsonPath("$.valores.subtotal").value(5500))
                .andExpect(jsonPath("$.valores.desconto").value(275))
                .andExpect(jsonPath("$.valores.imposto").value(550))
                .andExpect(jsonPath("$.valores.total").value(5775));

        var pedido = pedidos.porEstado(Estado.APROVADO).getFirst();
        assertEquals(CLIENTE, pedido.email());
        assertEquals("Rua Exemplo 10", pedido.endereco());
        assertEquals(5775L, pedido.totais().total());
        assertEquals(Map.of(1L, 1, 3L, 1, 5L, 1, 6L, 1), pedido.reserva());
        assertEquals(List.of(Estado.NOVO, Estado.APROVADO),
                pedido.historico().stream().map(evento -> evento.estado()).toList());
        assertEquals(29, estoque.porcoes(1L));
        assertEquals(30, estoque.porcoes(4L));

        mvc.perform(post("/pedidos").principal(() -> CLIENTE).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"endereco":"Rua Exemplo 10","itens":[
                          {"produtoId":1,"quantidade":16},{"produtoId":2,"quantidade":16}
                        ]}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("NEGADO"))
                .andExpect(jsonPath("$.pedidoId").doesNotExist())
                .andExpect(jsonPath("$.itensIndisponiveis[0]").value(1))
                .andExpect(jsonPath("$.itensIndisponiveis[1]").value(2));
        assertEquals(1, pedidos.porEstado(Estado.APROVADO).size());
        assertEquals(29, estoque.porcoes(1L));
        assertEquals(30, estoque.porcoes(4L));
        assertEquals(Set.of(1L, 2L), estoque.produtosBloqueados());
        mvc.perform(get("/cardapio/corrente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].disponivel").value(false))
                .andExpect(jsonPath("$.itens[1].disponivel").value(false));

        mvc.perform(post("/estoque/reposicoes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"ingredienteId\":1,\"quantidade\":5}"))
                .andExpect(status().isNoContent());
        assertEquals(34, estoque.porcoes(1L));
        assertEquals(Set.of(), estoque.produtosBloqueados());
        mvc.perform(get("/cardapio/corrente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].disponivel").value(true))
                .andExpect(jsonPath("$.itens[1].disponivel").value(true));
    }
}
