package com.bcopstein.ex4_lancheriaddd_v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.EstoqueRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.DescontosService;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:preferencias-tests;DB_CLOSE_DELAY=-1")
class CardapioPreferenciasTests {
    @Autowired WebApplicationContext aplicacao;
    @Autowired EstoqueRepository estoque;
    @Autowired DescontosService descontos;

    private MockMvc mvc;

    @BeforeEach
    void configurarMvc() {
        // A autorização terá testes próprios quando houver login.
        mvc = MockMvcBuilders.webAppContextSetup(aplicacao).build();
    }

    // Troca o cardápio, lê a escolha em outra requisição e verifica falta de estoque e bloqueio.
    @Test
    void mostraCardapioSelecionadoComDisponibilidade() throws Exception {
        mvc.perform(get("/cardapio/corrente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Cardapio de Agosto"))
                .andExpect(jsonPath("$.itens[1].indicacao").value(true));

        mvc.perform(put("/cardapio/corrente").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cardapioId\":2}"))
                .andExpect(status().isNoContent());
        mvc.perform(put("/cardapio/corrente").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cardapioId\":999}"))
                .andExpect(status().isNotFound());

        mvc.perform(get("/cardapio/corrente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Cardapio de Setembro"))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[0].preco").value(5500))
                .andExpect(jsonPath("$.itens[0].disponivel").value(true));

        int porcoesDeMassa = estoque.porcoes(1L);
        estoque.somarPorcoes(1L, -porcoesDeMassa);
        try {
            mvc.perform(get("/cardapio/corrente"))
                    .andExpect(jsonPath("$.itens[0].disponivel").value(false))
                    .andExpect(jsonPath("$.itens[1].disponivel").value(false));
        } finally {
            estoque.somarPorcoes(1L, porcoesDeMassa);
        }

        estoque.bloquear(1L);
        try {
            mvc.perform(get("/cardapio/corrente"))
                    .andExpect(jsonPath("$.itens[0].disponivel").value(false))
                    .andExpect(jsonPath("$.itens[1].disponivel").value(true));
        } finally {
            estoque.desbloquear(1L);
        }
    }

    // Lista as opções e comprova que o cálculo posterior usa a escolha persistida.
    @Test
    void listaPoliticasEPreservaASelecaoQuandoOCodigoInexiste() throws Exception {
        mvc.perform(get("/descontos/politicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo",
                        org.hamcrest.Matchers.contains("SemDesconto", "Fidelidade", "PromocaoVerao")));

        mvc.perform(put("/descontos/corrente").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"PromocaoVerao\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(put("/descontos/corrente").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"Inexistente\"}"))
                .andExpect(status().isNotFound());

        assertEquals(275L, descontos.calcular(List.of(5500L), 0));
    }
}
