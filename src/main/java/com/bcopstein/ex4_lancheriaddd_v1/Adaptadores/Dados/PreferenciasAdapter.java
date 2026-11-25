package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PreferenciasRepository;

@Repository
@Transactional(readOnly = true)
public class PreferenciasAdapter implements PreferenciasRepository {
    private final EntityManager banco;

    public PreferenciasAdapter(EntityManager banco) { this.banco = banco; }

    private PreferenciasEntity preferencias() { return banco.find(PreferenciasEntity.class, 1); }
    public long cardapioCorrente() { return preferencias().cardapioId; }
    public String descontoCorrente() { return preferencias().descontoCodigo; }

    @Transactional
    public void definirCardapio(long id) {
        bloquearAlteracoes();
        preferencias().cardapioId = id;
    }

    @Transactional
    public void definirDesconto(String codigo) {
        bloquearAlteracoes();
        preferencias().descontoCodigo = codigo;
    }

    @Transactional
    public void bloquearAlteracoes() {
        banco.find(PreferenciasEntity.class, 1, LockModeType.PESSIMISTIC_WRITE);
    }
}
