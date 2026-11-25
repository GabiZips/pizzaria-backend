package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import org.springframework.stereotype.Component;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.DescontosService;

@Component
public class DefinirPoliticaDescontoUC {
    private final DescontosService descontos;
    public DefinirPoliticaDescontoUC(DescontosService descontos) { this.descontos = descontos; }
    public void run(String codigo) { descontos.definir(codigo); }
}
