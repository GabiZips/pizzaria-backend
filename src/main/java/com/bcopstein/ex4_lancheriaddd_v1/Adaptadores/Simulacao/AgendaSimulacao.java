package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Simulacao;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.SimularOperacaoUC;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "pizzaria.simulacao.ativa", havingValue = "true", matchIfMissing = true)
public class AgendaSimulacao {
    private final SimularOperacaoUC operacao;
    public AgendaSimulacao(SimularOperacaoUC operacao) { this.operacao = operacao; }

    @Scheduled(fixedDelay = 2000, initialDelay = 2000)
    public void simular() {
        operacao.executarCiclo();
        operacao.expirarReservas();
    }
}
