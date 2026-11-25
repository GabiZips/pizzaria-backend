package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import org.springframework.stereotype.Service;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PreferenciasRepository;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DescontosService {
    public record Opcao(String codigo, String descricao) {}
    private final PreferenciasRepository preferencias;

    public DescontosService(PreferenciasRepository preferencias) { this.preferencias = preferencias; }

    public List<Opcao> listar() {
        return Arrays.stream(Descontos.values())
                .map(p -> new Opcao(p.codigo(), p.descricao())).toList();
    }

    private PoliticaDesconto localizar(String codigo) {
        return Arrays.stream(Descontos.values()).filter(p -> p.codigo().equals(codigo))
                .findFirst().orElseThrow(() -> new NoSuchElementException("Política inexistente"));
    }

    public void definir(String codigo) { preferencias.definirDesconto(localizar(codigo).codigo()); }

    public long calcular(List<Long> subtotais, long pedidosRecentes) {
        var politica = localizar(preferencias.descontoCorrente());
        return subtotais.stream().mapToLong(valor -> politica.calcular(valor, pedidosRecentes)).sum();
    }
}
