package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
public class TokenCsrfController {
    record Token(String headerName, String token) {}

    @GetMapping("/csrf")
    Token token(CsrfToken csrf) { return new Token(csrf.getHeaderName(), csrf.getToken()); }
}
