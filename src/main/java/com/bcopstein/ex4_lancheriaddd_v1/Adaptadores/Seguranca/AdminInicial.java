package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Seguranca;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.CadastrarUsuarioUC;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.UsuariosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.UsuariosRepository.Usuario;

@Configuration
public class AdminInicial {
    @Bean
    ApplicationRunner cadastrarAdmin(UsuariosRepository usuarios, PasswordEncoder encoder,
            @Value("${PIZZARIA_ADMIN_EMAIL:}") String email,
            @Value("${PIZZARIA_ADMIN_SENHA:}") String senha) {
        return args -> {
            if (email.isBlank() && senha.isBlank()) return;
            String login = CadastrarUsuarioUC.normalizarEmail(email);
            CadastrarUsuarioUC.validarSenha(senha);
            var existente = usuarios.porEmail(login);
            if (existente.isPresent()) {
                if (!"ADMIN".equals(existente.get().perfil())) {
                    throw new IllegalStateException("E-mail do administrador já usado");
                }
                return;
            }
            usuarios.cadastrar(new Usuario("00000000000", "Administrador", "Local", "Local",
                    login, encoder.encode(senha), "ADMIN"));
        };
    }
}
