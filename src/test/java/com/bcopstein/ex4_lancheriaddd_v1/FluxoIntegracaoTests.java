package com.bcopstein.ex4_lancheriaddd_v1;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.*;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.*;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Estado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:fluxo-teste;DB_CLOSE_DELAY=-1",
    "pizzaria.simulacao.ativa=false",
    "pizzaria.imposto.lei=2000",
    "PIZZARIA_ADMIN_EMAIL=admin@exemplo.com",
    "PIZZARIA_ADMIN_SENHA=SenhaTeste123!"
})
@AutoConfigureMockMvc
class FluxoIntegracaoTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired Clock relogio;
    @Autowired EstoqueRepository estoque;
    @Autowired PedidosRepository pedidos;
    @Autowired DescontosService descontos;
    @Autowired ImpostosService impostos;
    @Autowired SimularOperacaoUC simulacao;
    @Autowired PagarPedidoUC pagar;
    @Autowired UsuariosRepository usuarios;
    @Autowired PasswordEncoder encoder;

    // Cenário: três políticas, seleção persistida e imposto de 12% configurado no contexto.
    @Test
    void estrategiasServicosEBanco() {
        descontos.definir("SemDesconto");
        assertEquals(0, descontos.calcular(List.of(10_000L), 4));
        descontos.definir("Fidelidade");
        assertEquals(700, descontos.calcular(List.of(10_000L), 4));
        descontos.definir("PromocaoVerao");
        assertEquals(500, descontos.calcular(List.of(10_000L), 4));
        assertEquals(1_200, impostos.calcular(10_000L));
    }

    // Cenário: cadastro e login reais, autorização, falta de estoque, reposição, aprovação,
    // cancelamento, pagamento e entrega. Esperado: dados e estados recuperáveis por JPA.
    @Test
    void fluxoCompleto() throws Exception {
        mvc.perform(get("/cardapio/lista")).andExpect(status().isUnauthorized());
        var admin = login("admin@exemplo.com", "SenhaTeste123!");
        mvc.perform(get("/cardapio/lista").session(admin)).andExpect(status().isOk());
        var csrfAdmin = token(admin);
        mvc.perform(put("/cardapio/corrente").session(admin).header("X-CSRF-TOKEN", csrfAdmin)
                .contentType("application/json").content("{\"cardapioId\":2}"))
                .andExpect(status().isNoContent());
        mvc.perform(put("/descontos/corrente").session(admin).header("X-CSRF-TOKEN", token(admin))
                .contentType("application/json").content("{\"codigo\":\"SemDesconto\"}"))
                .andExpect(status().isNoContent());
        var anonimo = new MockHttpSession();
        mvc.perform(post("/usuarios").session(anonimo).header("X-CSRF-TOKEN", token(anonimo))
                .contentType("application/json").content("""
                    {"cpf":"12345678901","nome":"Cliente teste","telefone":"51999999999",
                     "endereco":"Rua Exemplo 10","email":"cliente@exemplo.com","senha":"SenhaCliente123!"}
                    """)).andExpect(status().isCreated()).andExpect(jsonPath("$.senha").doesNotExist());

        var cliente = login("cliente@exemplo.com", "SenhaCliente123!");
        mvc.perform(get("/cardapio/lista").session(cliente)).andExpect(status().isForbidden());
        mvc.perform(get("/cardapio/corrente").session(cliente))
                .andExpect(jsonPath("$.titulo").value("Cardapio de Setembro"));
        pedir(cliente, 31, 409);
        assertEquals(30, estoque.porcoes(1L));
        mvc.perform(get("/cardapio/corrente").session(cliente))
                .andExpect(jsonPath("$.itens[0].disponivel").value(false));
        mvc.perform(post("/estoque/reposicoes").session(admin).header("X-CSRF-TOKEN", token(admin))
                .contentType("application/json").content("{\"ingredienteId\":1,\"quantidade\":1}"))
                .andExpect(status().isNoContent());
        long cancelado = pedir(cliente, 1, 201);
        assertEquals(6_160L, pedidos.buscar(cancelado).orElseThrow().totais().total());
        mvc.perform(post("/pedidos/{id}/cancelamento", cancelado).session(cliente)
                .header("X-CSRF-TOKEN", token(cliente))).andExpect(status().isNoContent());
        assertEquals(31, estoque.porcoes(1L));

        long id = pedir(cliente, 1, 201);
        mvc.perform(post("/pedidos/{id}/pagamento", id).session(cliente)
                .header("X-CSRF-TOKEN", token(cliente)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAGO"));
        mvc.perform(post("/pedidos/{id}/pagamento", id).session(cliente)
                .header("X-CSRF-TOKEN", token(cliente)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pedidoId").value(id));
        mvc.perform(post("/pedidos/{id}/cancelamento", id).session(cliente)
                .header("X-CSRF-TOKEN", token(cliente))).andExpect(status().isConflict());
        for (int ciclo = 0; ciclo < 5; ciclo++) simulacao.executarCiclo();
        mvc.perform(get("/pedidos/{id}/status", id).session(cliente))
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.entregador").value("Entregador simulado 1"));
        var entregue = pedidos.buscar(id).orElseThrow();
        assertNotNull(entregue.entregueEm());
        assertEquals(List.of(Estado.NOVO, Estado.APROVADO, Estado.PAGO, Estado.AGUARDANDO,
                Estado.PREPARACAO, Estado.PRONTO, Estado.TRANSPORTE, Estado.ENTREGUE),
                entregue.historico().stream().map(PedidoRegistrado.Evento::estado).toList());
        String hoje = LocalDate.now(relogio).toString();
        mvc.perform(get("/pedidos/entregues").session(cliente).param("inicio", hoje).param("fim", hoje))
                .andExpect(jsonPath("$[0].pedidoId").value(id));
        String ontem = LocalDate.now(relogio).minusDays(1).toString();
        mvc.perform(get("/pedidos/entregues").session(cliente).param("inicio", ontem).param("fim", ontem))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/pedidos/entregues").session(cliente).param("inicio", hoje).param("fim", ontem))
                .andExpect(status().isBadRequest());

        var outraSessao = new MockHttpSession();
        cadastrar(outraSessao, "12345678902", "outra@exemplo.com", 201);
        var outraCliente = login("outra@exemplo.com", "SenhaCliente123!");
        mvc.perform(get("/pedidos/{id}/status", id).session(outraCliente))
                .andExpect(status().isNotFound());
        mvc.perform(get("/pedidos/entregues").session(outraCliente).param("inicio", hoje).param("fim", hoje))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    // Cenário: CPF/e-mail repetidos, senha incorreta, POST sem CSRF e saída da sessão.
    @Test
    void cadastroELoginProtegemAContaESessao() throws Exception {
        var anonimo = new MockHttpSession();
        cadastrar(anonimo, "12345678903", "Seguranca@exemplo.com", 201);
        cadastrar(anonimo, "12345678903", "outro-email@exemplo.com", 409);
        cadastrar(anonimo, "12345678904", "SEGURANCA@exemplo.com", 409);
        var gravado = usuarios.porEmail("seguranca@exemplo.com").orElseThrow();
        assertEquals("CLIENTE", gravado.perfil());
        assertNotEquals("SenhaCliente123!", gravado.senhaHash());
        assertTrue(encoder.matches("SenhaCliente123!", gravado.senhaHash()));

        mvc.perform(post("/login").session(anonimo).header("X-CSRF-TOKEN", token(anonimo))
                .param("username", "seguranca@exemplo.com").param("password", "SenhaErrada123!"))
                .andExpect(status().isUnauthorized());
        var sessao = login("SEGURANCA@exemplo.com", "SenhaCliente123!");
        mvc.perform(post("/pedidos").session(sessao).contentType("application/json")
                .content("{\"endereco\":\"Rua Exemplo\",\"itens\":[{\"produtoId\":1,\"quantidade\":1}]}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/logout").session(sessao).header("X-CSRF-TOKEN", token(sessao)))
                .andExpect(status().isNoContent());
        assertTrue(sessao.isInvalid());
        mvc.perform(get("/cardapio/corrente")).andExpect(status().isUnauthorized());
    }

    // Cenário: uma reserva de 31 minutos fica fora do pagamento e é liberada uma vez na expiração.
    @Test
    void liberaReservaExpiradaSemAceitarPagamento() {
        int saldoOriginal = estoque.porcoes(1L);
        estoque.somarPorcoes(1L, -1);
        var aprovadoEm = LocalDateTime.now(relogio).minusMinutes(31);
        var reserva = pedidos.salvar(new PedidoRegistrado(null, "reserva@exemplo.com", "Rua Exemplo",
                List.of(new PedidoRegistrado.Linha(1L, 1, 5500L)),
                new PedidoRegistrado.Totais(5500L, 0L, 550L, 6050L), Map.of(1L, 1),
                Estado.APROVADO, aprovadoEm, null, null, null,
                List.of(new PedidoRegistrado.Evento(Estado.APROVADO, aprovadoEm))));

        assertThrows(IllegalStateException.class, () -> pagar.run(reserva.email(), reserva.id()));
        simulacao.expirarReservas();
        simulacao.expirarReservas();
        var cancelado = pedidos.buscar(reserva.id()).orElseThrow();
        assertEquals(Estado.CANCELADO, cancelado.estado());
        assertEquals(2, cancelado.historico().size());
        assertEquals(saldoOriginal, estoque.porcoes(1L));
    }

    private MockHttpSession login(String email, String senha) throws Exception {
        var sessao = new MockHttpSession();
        var resposta = mvc.perform(post("/login").session(sessao).header("X-CSRF-TOKEN", token(sessao))
                        .param("username", email).param("password", senha))
                .andExpect(status().isNoContent()).andReturn();
        return (MockHttpSession) resposta.getRequest().getSession(false);
    }

    private String token(MockHttpSession sessao) throws Exception {
        var resposta = mvc.perform(get("/csrf").session(sessao)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).path("token").asText();
    }

    private void cadastrar(MockHttpSession sessao, String cpf, String email, int esperado) throws Exception {
        String cadastro = """
                {"cpf":"%s","nome":"Cliente teste","telefone":"51999999999","endereco":"Rua Exemplo",
                 "email":"%s","senha":"SenhaCliente123!","perfil":"ADMIN"}
                """.formatted(cpf, email);
        mvc.perform(post("/usuarios").session(sessao).header("X-CSRF-TOKEN", token(sessao))
                .contentType("application/json").content(cadastro))
                .andExpect(status().is(esperado))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    private long pedir(MockHttpSession sessao, int quantidade, int esperado) throws Exception {
        String corpo = """
                {"endereco":"Rua Exemplo 10","itens":[{"produtoId":1,"quantidade":%d}]}
                """.formatted(quantidade);
        var resposta = mvc.perform(post("/pedidos").session(sessao).header("X-CSRF-TOKEN", token(sessao))
                .contentType("application/json").content(corpo))
                .andExpect(status().is(esperado)).andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).path("pedidoId").asLong();
    }
}
