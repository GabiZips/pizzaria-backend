package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.ListarEntreguesUC;

@RestController
public class EntreguesController {
    private final ListarEntreguesUC listar;
    public EntreguesController(ListarEntreguesUC listar) { this.listar = listar; }

    record Resumo(long pedidoId, LocalDateTime entregueEm, long total) {}

    @GetMapping("/pedidos/entregues")
    List<Resumo> listar(Principal pessoa,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return listar.run(ClienteDaRequisicao.email(pessoa), inicio, fim).stream()
                .map(p -> new Resumo(p.id(), p.entregueEm(), p.totais().total())).toList();
    }
}
