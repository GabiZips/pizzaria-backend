package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.CadastrarUsuarioUC;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.UsuariosRepository;

@Configuration
public class SegurancaApi {
    @Bean
    UserDetailsService usuariosParaLogin(UsuariosRepository usuarios) {
        return informado -> {
            String email;
            try {
                email = CadastrarUsuarioUC.normalizarEmail(informado);
            } catch (IllegalArgumentException erro) {
                throw new UsernameNotFoundException("Usuário não encontrado");
            }
            var usuario = usuarios.porEmail(email)
                    .filter(u -> u.senhaHash() != null && u.perfil() != null)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
            return User.withUsername(usuario.email()).password(usuario.senhaHash())
                    .roles(usuario.perfil()).build();
        };
    }

    @Bean
    SecurityFilterChain rotas(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios", "/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/cardapio/lista", "/descontos/politicas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/cardapio/corrente", "/descontos/corrente").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/estoque/reposicoes").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cardapio/corrente").authenticated()
                        .requestMatchers("/cardapio/*").hasRole("ADMIN")
                        .requestMatchers("/pedidos", "/pedidos/**").hasRole("CLIENTE")
                        .anyRequest().denyAll())
                .formLogin(login -> login.loginProcessingUrl("/login")
                        .successHandler((req, res, auth) -> res.setStatus(204))
                        .failureHandler((req, res, erro) -> res.setStatus(401)))
                .logout(logout -> logout.logoutSuccessHandler((req, res, auth) -> res.setStatus(204)))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .requestCache(cache -> cache.disable())
                .build();
    }
}
