package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.DefinirPoliticaDescontoUC;

@RestController
public class DefinirDescontoController {
    private final DefinirPoliticaDescontoUC definir;
    public DefinirDescontoController(DefinirPoliticaDescontoUC definir) { this.definir = definir; }

    record Selecao(String codigo) {}

    @PutMapping("/descontos/corrente")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void atualizar(@RequestBody Selecao entrada) { definir.run(entrada.codigo()); }
}
