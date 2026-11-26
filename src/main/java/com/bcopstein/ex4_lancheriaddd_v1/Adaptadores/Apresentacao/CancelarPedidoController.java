package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.CancelarPedidoUC;

@RestController
public class CancelarPedidoController {
    private final CancelarPedidoUC cancelar;

    public CancelarPedidoController(CancelarPedidoUC cancelar) {
        this.cancelar = cancelar;
    }

    @PostMapping("/pedidos/{id}/cancelamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelar(@PathVariable long id, Principal pessoa) {
        cancelar.run(ClienteDaRequisicao.email(pessoa), id);
    }
}
