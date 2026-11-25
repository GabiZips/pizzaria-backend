package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.DescontosService;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.ListarPoliticasDescontoUC;

@RestController
public class ListarDescontosController {
    private final ListarPoliticasDescontoUC listar;
    public ListarDescontosController(ListarPoliticasDescontoUC listar) { this.listar = listar; }

    @GetMapping("/descontos/politicas")
    List<DescontosService.Opcao> listar() { return listar.run(); }
}
