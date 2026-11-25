package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.util.List;
import org.springframework.stereotype.Component;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.DescontosService;

@Component
public class ListarPoliticasDescontoUC {
    private final DescontosService descontos;
    public ListarPoliticasDescontoUC(DescontosService descontos) { this.descontos = descontos; }
    public List<DescontosService.Opcao> run() { return descontos.listar(); }
}
