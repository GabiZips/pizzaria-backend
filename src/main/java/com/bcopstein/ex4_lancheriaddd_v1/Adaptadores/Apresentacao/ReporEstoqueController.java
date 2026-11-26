package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.ReporEstoqueUC;

@RestController
public class ReporEstoqueController {
    private final ReporEstoqueUC repor;
    public ReporEstoqueController(ReporEstoqueUC repor) { this.repor = repor; }

    record Reposicao(long ingredienteId, int quantidade) {}

    @PostMapping("/estoque/reposicoes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void repor(@RequestBody Reposicao entrada) { repor.run(entrada.ingredienteId(), entrada.quantidade()); }
}
