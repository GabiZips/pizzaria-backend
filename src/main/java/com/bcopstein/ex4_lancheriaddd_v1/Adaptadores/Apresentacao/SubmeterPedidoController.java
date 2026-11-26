package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.SubmeterPedidoUC;

@RestController
public class SubmeterPedidoController {
    private final SubmeterPedidoUC submeter;
    public SubmeterPedidoController(SubmeterPedidoUC submeter) { this.submeter = submeter; }

    record Entrada(String endereco, List<SubmeterPedidoUC.Item> itens) {}

    @PostMapping("/pedidos")
    ResponseEntity<SubmeterPedidoUC.Resultado> submeter(@RequestBody Entrada entrada, Principal pessoa) {
        var resultado = submeter.run(ClienteDaRequisicao.email(pessoa), entrada.endereco(), entrada.itens());
        HttpStatus status = resultado.pedidoId() == null ? HttpStatus.CONFLICT : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(resultado);
    }
}
