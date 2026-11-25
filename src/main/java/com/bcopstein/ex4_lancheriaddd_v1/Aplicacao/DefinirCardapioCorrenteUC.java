package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Component;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PreferenciasRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.CardapioRepository;

@Component
public class DefinirCardapioCorrenteUC {
    private final CardapioRepository cardapios;
    private final PreferenciasRepository preferencias;

    public DefinirCardapioCorrenteUC(CardapioRepository cardapios, PreferenciasRepository preferencias) {
        this.cardapios = cardapios;
        this.preferencias = preferencias;
    }

    public void run(long id) {
        if (cardapios.recuperaPorId(id) == null) {
            throw new java.util.NoSuchElementException("Cardápio inexistente");
        }
        preferencias.definirCardapio(id);
    }
}
