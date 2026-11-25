package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.DefinirCardapioCorrenteUC;

@RestController
public class DefinirCardapioController {
    private final DefinirCardapioCorrenteUC definir;

    public DefinirCardapioController(DefinirCardapioCorrenteUC definir) { this.definir = definir; }

    record Selecao(long cardapioId) {}

    @PutMapping("/cardapio/corrente")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void atualizar(@RequestBody Selecao entrada) {
        definir.run(entrada.cardapioId());
    }
}
