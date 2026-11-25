package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.CarregarCardapioCorrenteUC;

@RestController
public class CardapioCorrenteController {
    private final CarregarCardapioCorrenteUC carregar;
    public CardapioCorrenteController(CarregarCardapioCorrenteUC carregar) { this.carregar = carregar; }

    @GetMapping("/cardapio/corrente")
    CarregarCardapioCorrenteUC.Menu consultar() { return carregar.run(); }
}
