package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ImpostosService {
    // Códigos didáticos. Troque pelos números de lei informados na disciplina.
    private static final Map<String, Integer> TAXAS = Map.of("1000", 10, "2000", 12);
    private final int taxa;

    public ImpostosService(@Value("${pizzaria.imposto.lei:1000}") String lei) {
        Integer taxaSelecionada = TAXAS.get(lei);
        if (taxaSelecionada == null) throw new IllegalArgumentException("Código de imposto inválido");
        this.taxa = taxaSelecionada;
    }

    public long calcular(long subtotal) { return Valores.percentual(subtotal, taxa); }
}
