package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

public interface PreferenciasRepository {
    long cardapioCorrente();
    String descontoCorrente();
    void definirCardapio(long id);
    void definirDesconto(String codigo);
    void bloquearAlteracoes();
}
