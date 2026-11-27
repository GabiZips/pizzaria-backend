package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.stereotype.Component;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Estado;

@Component
public class CozinhaService implements ICozinhaService {
    public Estado proximoEstado(Estado atual) {
        return switch (atual) {
            case PAGO -> Estado.AGUARDANDO;
            case AGUARDANDO -> Estado.PREPARACAO;
            case PREPARACAO -> Estado.PRONTO;
            default -> throw new IllegalArgumentException("Estado inválido para a cozinha");
        };
    }
}
