package com.bcopstein.ex4_lancheriaddd_v1;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.SubmeterPedidoUC;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.*;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.*;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.*;

class RegrasUnitariasTests {
    // Cenário: três estratégias. A fidelidade é zero com três pedidos e 7% com quatro.
    @Test
    void estrategiasDeDesconto() {
        assertEquals(0, Descontos.SEM_DESCONTO.calcular(10_000, 9));
        assertEquals(0, Descontos.FIDELIDADE.calcular(10_000, 3));
        assertEquals(700, Descontos.FIDELIDADE.calcular(10_000, 4));
        assertEquals(500, Descontos.PROMOCAO.calcular(10_000, 0));
    }

    // Cenário: serviço usa a política corrente e aplica o desconto em cada linha.
    @Test
    void servicoDeDescontos() {
        var preferencias = mock(PreferenciasRepository.class);
        when(preferencias.descontoCorrente()).thenReturn("Fidelidade");
        var servico = new DescontosService(preferencias);
        assertEquals(3, servico.listar().size());
        assertEquals(700, servico.calcular(List.of(5_000L, 5_000L), 4));
        assertThrows(java.util.NoSuchElementException.class, () -> servico.definir("Inexistente"));
        verify(preferencias, never()).definirDesconto(anyString());
    }

    // Cenário: dois códigos válidos produzem valores distintos e código desconhecido é recusado.
    @Test
    void servicoDeImpostos() {
        assertEquals(1_000, new ImpostosService("1000").calcular(10_000));
        assertEquals(1_200, new ImpostosService("2000").calcular(10_000));
        assertThrows(IllegalArgumentException.class, () -> new ImpostosService("9999"));
    }

    // Cenário: dois produtos compartilham ingrediente com uma porção. Negar sem baixar estoque.
    @Test
    void submeterComFaltaCompartilhada() {
        var preferencias = mock(PreferenciasRepository.class);
        var cardapios = mock(CardapioRepository.class);
        var estoque = mock(EstoqueRepository.class);
        var pedidos = mock(PedidosRepository.class);
        var precos = mock(PedidosService.class);
        var receita = new Receita(1L, "Receita", List.of(new Ingrediente(1L, "Massa")));
        var cardapio = new Cardapio(new CabecalhoCardapio(1L, "Teste"),
                List.of(new Produto(1L, "Uma", receita, 500), new Produto(2L, "Duas", receita, 700)));
        when(preferencias.cardapioCorrente()).thenReturn(1L);
        when(cardapios.recuperaPorId(1L)).thenReturn(cardapio);
        when(estoque.porcoes(1L)).thenReturn(1);
        when(estoque.produtosBloqueados()).thenReturn(Set.of());
        var uc = new SubmeterPedidoUC(cardapios, preferencias, estoque, new EstoqueService(estoque),
                pedidos, precos, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        var resultado = uc.run("cliente@exemplo.com", "Rua Exemplo",
                List.of(new SubmeterPedidoUC.Item(1L, 1), new SubmeterPedidoUC.Item(2L, 1)));

        assertEquals("NEGADO", resultado.status());
        assertEquals(List.of(1L, 2L), resultado.itensIndisponiveis());
        verify(estoque).bloquear(1L);
        verify(estoque).bloquear(2L);
        verify(estoque, never()).somarPorcoes(anyLong(), anyInt());
        verifyNoInteractions(pedidos, precos);
    }
}
