package com.bcopstein.ex4_lancheriaddd_v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.CardapioRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.IngredientesRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ReceitasRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.CabecalhoCardapio;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Ingrediente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;

// Usa um banco separado para nao compartilhar os dados com outros contextos de teste.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pizzadb-jpa-tests;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class PersistenciaJPATests {

    @Autowired
    private IngredientesRepository ingredientes;

    @Autowired
    private ReceitasRepository receitas;

    @Autowired
    private ProdutosRepository produtos;

    @Autowired
    private CardapioRepository cardapios;

    @Autowired
    private MockMvc mvc;

    /* Caso: listar os ingredientes iniciais e filtrar os usados na receita 1.
     * Esperado: nove ingredientes; receita 1 com disco, queijo, calabresa e molho.
     */
    @Test
    void deveConsultarIngredientesPelosAdaptadores() {
        var todos = ingredientes.recuperaTodos();
        assertEquals(9, todos.size());
        assertEquals("Disco de pizza", todos.getFirst().getDescricao());
        assertEquals(List.of(1L, 3L, 5L, 6L),
                ingredientes.recuperaIngredientesReceita(1L).stream()
                        .map(Ingrediente::getId).toList());
    }

    /* Caso: carregar uma receita junto dos seus ingredientes.
     * Esperado: os dados relacionados continuam acessiveis depois do retorno do adaptador.
     */
    @Test
    void deveCarregarReceitaComIngredientes() {
        var receita = receitas.recuperaReceita(2L);
        assertNotNull(receita);
        assertEquals("Pizza queijo e presunto", receita.getTitulo());
        assertEquals(List.of(1L, 3L, 4L, 6L),
                receita.getIngredientes().stream().map(Ingrediente::getId).toList());
    }

    /* Caso: buscar produto por ID e os produtos de cada cardapio.
     * Esperado: manter preco em centavos, receita e separacao entre os cardapios.
     */
    @Test
    void deveCarregarProdutosComReceitaEFiltrarPorCardapio() {
        var produto = produtos.recuperaProdutoPorid(1L);
        assertNotNull(produto);
        assertEquals(5500, produto.getPreco());
        assertEquals(1L, produto.getReceita().getId());
        assertEquals(4, produto.getReceita().getIngredientes().size());
        assertEquals(List.of(1L, 2L, 3L), produtos.recuperaProdutosCardapio(1L).stream()
                .map(Produto::getId).toList());
        assertEquals(List.of(1L, 3L), produtos.recuperaProdutosCardapio(2L).stream()
                .map(Produto::getId).toList());
    }

    /* Caso: listar os cabecalhos, carregar cardapio completo e consultar indicacao do chef.
     * Esperado: mesmos dados apresentados pelo estudo de caso.
     */
    @Test
    void deveCarregarCardapiosESugestaoDoChef() {
        assertEquals(List.of(new CabecalhoCardapio(1L, "Cardapio de Agosto"),
                new CabecalhoCardapio(2L, "Cardapio de Setembro")),
                cardapios.cardapiosDisponiveis());
        var cardapio = cardapios.recuperaPorId(1L);
        assertNotNull(cardapio);
        assertEquals("Cardapio de Agosto", cardapio.getCabecalhoCardapio().titulo());
        assertEquals(3, cardapio.getProdutos().size());
        assertEquals(4, cardapio.getProdutos().getFirst().getReceita().getIngredientes().size());
        assertEquals(2L, cardapios.indicacoesDoChef().getFirst().getId());
    }

    /* Caso: consultar IDs que nao existem.
     * Esperado: preservar os retornos null e listas vazias das interfaces existentes.
     */
    @Test
    void devePreservarRetornosParaIdsInexistentes() {
        assertNull(receitas.recuperaReceita(999L));
        assertNull(produtos.recuperaProdutoPorid(999L));
        assertNull(cardapios.recuperaPorId(999L));
        assertTrue(ingredientes.recuperaIngredientesReceita(999L).isEmpty());
        assertTrue(produtos.recuperaProdutosCardapio(999L).isEmpty());
    }

    /* Caso: acessar as rotas existentes pelo fluxo HTTP simulado.
     * Esperado: mesmo JSON, precos e indicacoes, usando agora os adaptadores JPA.
     */
    @Test
    void devePreservarRespostasDasConsultasDeCardapio() throws Exception {
        mvc.perform(get("/cardapio/lista"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":1,"titulo":"Cardapio de Agosto"},
                         {"id":2,"titulo":"Cardapio de Setembro"}]
                        """, JsonCompareMode.STRICT));

        mvc.perform(get("/cardapio/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"titulo":"Cardapio de Agosto","itens":[
                          {"id":1,"descricao":"Pizza calabresa","preco":5500,"indicacao":false},
                          {"id":2,"descricao":"Pizza queijo e presunto","preco":6000,"indicacao":true},
                          {"id":3,"descricao":"Pizza margherita","preco":4000,"indicacao":false}
                        ]}
                        """, JsonCompareMode.STRICT));

        mvc.perform(get("/cardapio/2"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"titulo":"Cardapio de Setembro","itens":[
                          {"id":1,"descricao":"Pizza calabresa","preco":5500,"indicacao":false},
                          {"id":3,"descricao":"Pizza margherita","preco":4000,"indicacao":false}
                        ]}
                        """, JsonCompareMode.STRICT));
    }
}
