package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.CadastrarUsuarioUC;

@RestController
public class CadastroController {
    private final CadastrarUsuarioUC cadastro;
    public CadastroController(CadastrarUsuarioUC cadastro) { this.cadastro = cadastro; }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    CadastrarUsuarioUC.Cadastrado cadastrar(@RequestBody CadastrarUsuarioUC.Cadastro entrada) {
        return cadastro.run(entrada);
    }
}
