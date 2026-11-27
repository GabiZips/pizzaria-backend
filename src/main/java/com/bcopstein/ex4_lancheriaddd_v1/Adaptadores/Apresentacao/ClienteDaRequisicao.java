package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import java.security.Principal;

final class ClienteDaRequisicao {
    private ClienteDaRequisicao() {}

    static String email(Principal principal) { return principal.getName(); }
}
