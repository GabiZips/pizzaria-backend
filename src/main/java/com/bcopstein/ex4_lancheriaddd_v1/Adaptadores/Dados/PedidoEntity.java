package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Estado;

@Entity
@Table(name = "pedidos")
public class PedidoEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String email;
    String endereco;
    long subtotal;
    long desconto;
    long imposto;
    long total;
    @Enumerated(EnumType.STRING)
    Estado estado;
    LocalDateTime aprovadoEm;
    LocalDateTime pagoEm;
    LocalDateTime entregueEm;
    String entregador;

    @ElementCollection
    @CollectionTable(name = "pedido_itens", joinColumns = @JoinColumn(name = "pedido_id"))
    @OrderColumn(name = "posicao")
    List<Item> itens = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "pedido_reserva", joinColumns = @JoinColumn(name = "pedido_id"))
    @MapKeyColumn(name = "ingrediente_id")
    @Column(name = "quantidade")
    Map<Long, Integer> reserva = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "pedido_historico", joinColumns = @JoinColumn(name = "pedido_id"))
    @OrderColumn(name = "posicao")
    List<Evento> historico = new ArrayList<>();

    protected PedidoEntity() {}

    @Embeddable
    public static class Item {
        long produtoId;
        int quantidade;
        long precoUnitario;
        protected Item() {}
        Item(long produtoId, int quantidade, long precoUnitario) {
            this.produtoId = produtoId;
            this.quantidade = quantidade;
            this.precoUnitario = precoUnitario;
        }
    }

    @Embeddable
    public static class Evento {
        @Enumerated(EnumType.STRING)
        Estado estado;
        LocalDateTime quando;
        protected Evento() {}
        Evento(Estado estado, LocalDateTime quando) {
            this.estado = estado;
            this.quando = quando;
        }
    }
}
