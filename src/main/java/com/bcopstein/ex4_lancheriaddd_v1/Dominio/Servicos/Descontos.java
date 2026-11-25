package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

public enum Descontos implements PoliticaDesconto {
    SEM_DESCONTO("SemDesconto", "Sem promoção") {
        public long calcular(long valor, long pedidos) { return 0; }
    },
    FIDELIDADE("Fidelidade", "7% com mais de três pedidos recentes") {
        public long calcular(long valor, long pedidos) {
            return pedidos > 3 ? Valores.percentual(valor, 7) : 0;
        }
    },
    PROMOCAO("PromocaoVerao", "5% em cada item") {
        public long calcular(long valor, long pedidos) { return Valores.percentual(valor, 5); }
    };

    private final String codigo;
    private final String descricao;

    Descontos(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String codigo() { return codigo; }
    public String descricao() { return descricao; }
}
