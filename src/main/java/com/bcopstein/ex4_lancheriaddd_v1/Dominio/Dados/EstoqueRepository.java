package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import java.util.Set;

public interface EstoqueRepository {
    int porcoes(long ingredienteId);
    void somarPorcoes(long ingredienteId, int quantidade);
    Set<Long> produtosBloqueados();
    void bloquear(long produtoId);
    void desbloquear(long produtoId);
}
