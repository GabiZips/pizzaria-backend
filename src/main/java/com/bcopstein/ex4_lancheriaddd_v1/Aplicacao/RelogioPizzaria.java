package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelogioPizzaria {
    @Bean
    Clock relogio() { return Clock.system(ZoneId.of("America/Sao_Paulo")); }
}
