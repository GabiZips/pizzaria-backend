package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.stereotype.Service;
import java.util.List;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Linha;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.PedidoRegistrado.Totais;

@Service
public class PedidosService {
    private final ImpostosService impostos;
    private final DescontosService descontos;

    public PedidosService(ImpostosService impostos, DescontosService descontos) {
        this.impostos = impostos;
        this.descontos = descontos;
    }

    public Totais calcular(List<Linha> itens, long pedidosRecentes) {
        var linhas = itens.stream().map(i -> i.precoUnitario() * i.quantidade()).toList();
        long subtotal = linhas.stream().mapToLong(Long::longValue).sum();
        long desconto = descontos.calcular(linhas, pedidosRecentes);
        long imposto = impostos.calcular(subtotal);
        return new Totais(subtotal, desconto, imposto, subtotal - desconto + imposto);
    }
}
