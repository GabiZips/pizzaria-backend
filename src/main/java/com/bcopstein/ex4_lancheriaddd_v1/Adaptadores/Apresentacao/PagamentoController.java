package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.PagarPedidoUC;

@RestController
public class PagamentoController {
    private final PagarPedidoUC pagar;
    public PagamentoController(PagarPedidoUC pagar) { this.pagar = pagar; }

    @PostMapping("/pedidos/{id}/pagamento")
    PagarPedidoUC.Confirmacao pagar(@PathVariable long id, Principal pessoa) {
        return pagar.run(ClienteDaRequisicao.email(pessoa), id);
    }
}
