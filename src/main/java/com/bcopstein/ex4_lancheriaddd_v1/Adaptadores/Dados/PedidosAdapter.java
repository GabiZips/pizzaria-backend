package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.*;

@Repository
@Transactional(readOnly = true)
public class PedidosAdapter implements PedidosRepository {
    private final EntityManager banco;
    public PedidosAdapter(EntityManager banco) { this.banco = banco; }

    @Transactional
    public PedidoRegistrado salvar(PedidoRegistrado pedido) {
        boolean novo = pedido.id() == null;
        var linha = novo ? new PedidoEntity() : banco.find(PedidoEntity.class, pedido.id());
        if (novo) {
            linha.email = pedido.email();
            linha.endereco = pedido.endereco();
            linha.subtotal = pedido.totais().subtotal();
            linha.desconto = pedido.totais().desconto();
            linha.imposto = pedido.totais().imposto();
            linha.total = pedido.totais().total();
            linha.aprovadoEm = pedido.aprovadoEm();
            for (var item : pedido.itens()) {
                linha.itens.add(new PedidoEntity.Item(item.produtoId(), item.quantidade(), item.precoUnitario()));
            }
            linha.reserva.putAll(pedido.reserva());
        }
        linha.estado = pedido.estado();
        linha.pagoEm = pedido.pagoEm();
        linha.entregueEm = pedido.entregueEm();
        linha.entregador = pedido.entregador();
        for (int i = linha.historico.size(); i < pedido.historico().size(); i++) {
            var evento = pedido.historico().get(i);
            linha.historico.add(new PedidoEntity.Evento(evento.estado(), evento.quando()));
        }
        if (novo) banco.persist(linha);
        return converter(linha);
    }

    public Optional<PedidoRegistrado> buscar(long id) {
        return Optional.ofNullable(banco.find(PedidoEntity.class, id)).map(this::converter);
    }

    public long contarPagosDesde(String email, LocalDateTime inicio, LocalDateTime fim) {
        return banco.createQuery("""
                select count(p) from PedidoEntity p
                where p.email = :email and p.pagoEm >= :inicio and p.pagoEm <= :fim
                """, Long.class).setParameter("email", email)
                .setParameter("inicio", inicio).setParameter("fim", fim).getSingleResult();
    }

    public List<PedidoRegistrado> porEstado(Estado estado) {
        return banco.createQuery("from PedidoEntity p where p.estado = :estado order by p.pagoEm, p.id",
                PedidoEntity.class).setParameter("estado", estado).getResultList().stream()
                .map(this::converter).toList();
    }

    @Override
    public List<Long> idsDeAprovadosAte(LocalDateTime limite) {
        return banco.createQuery("""
                select p.id from PedidoEntity p
                where p.estado = :estado and p.pagoEm is null and p.aprovadoEm <= :limite
                order by p.aprovadoEm, p.id
                """, Long.class)
                .setParameter("estado", Estado.APROVADO)
                .setParameter("limite", limite)
                .getResultList();
    }

    public List<PedidoRegistrado> entregues(String email, LocalDateTime de, LocalDateTime ateExclusivo) {
        return banco.createQuery("""
                from PedidoEntity p where p.email = :email and p.estado = :estado
                and p.entregueEm >= :de and p.entregueEm < :ate order by p.entregueEm, p.id
                """, PedidoEntity.class)
                .setParameter("email", email).setParameter("estado", Estado.ENTREGUE)
                .setParameter("de", de).setParameter("ate", ateExclusivo)
                .getResultList().stream().map(this::converter).toList();
    }

    private PedidoRegistrado converter(PedidoEntity p) {
        var itens = p.itens.stream().map(i -> new Linha(i.produtoId, i.quantidade, i.precoUnitario)).toList();
        var eventos = p.historico.stream().map(e -> new Evento(e.estado, e.quando)).toList();
        return new PedidoRegistrado(p.id, p.email, p.endereco, itens,
                new Totais(p.subtotal, p.desconto, p.imposto, p.total), Map.copyOf(p.reserva),
                p.estado, p.aprovadoEm, p.pagoEm, p.entregueEm, p.entregador, eventos);
    }
}
