package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.UsuariosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.UsuariosRepository.Usuario;

@Component
public class CadastrarUsuarioUC {
    public record Cadastro(String cpf, String nome, String telefone, String endereco, String email, String senha) {}
    public record Cadastrado(String email, String nome) {}

    private final UsuariosRepository usuarios;
    private final PasswordEncoder encoder;

    public CadastrarUsuarioUC(UsuariosRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional
    public Cadastrado run(Cadastro entrada) {
        if (entrada.cpf() == null || !entrada.cpf().matches("[0-9]{11}")
                || entrada.cpf().equals("00000000000")) {
            throw new IllegalArgumentException("Informe 11 dígitos de CPF");
        }
        String nome = texto(entrada.nome(), 100, "nome");
        String telefone = texto(entrada.telefone(), 20, "telefone");
        String endereco = texto(entrada.endereco(), 255, "endereço");
        String email = normalizarEmail(entrada.email());
        validarSenha(entrada.senha());
        if (usuarios.existeCpf(entrada.cpf()) || usuarios.porEmail(email).isPresent()) {
            throw new IllegalStateException("CPF ou e-mail já cadastrado");
        }
        usuarios.cadastrar(new Usuario(entrada.cpf(), nome, telefone, endereco, email,
                encoder.encode(entrada.senha()), "CLIENTE"));
        return new Cadastrado(email, nome);
    }

    public static String normalizarEmail(String valor) {
        String email = texto(valor, 255, "e-mail").toLowerCase(Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("E-mail inválido");
        }
        return email;
    }

    public static void validarSenha(String senha) {
        if (senha == null || senha.isBlank() || senha.length() < 8
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Use uma senha com pelo menos 8 caracteres e até 72 bytes");
        }
    }

    private static String texto(String valor, int limite, String campo) {
        if (valor == null || valor.isBlank() || valor.strip().length() > limite) {
            throw new IllegalArgumentException("Campo inválido: " + campo);
        }
        return valor.strip();
    }
}
