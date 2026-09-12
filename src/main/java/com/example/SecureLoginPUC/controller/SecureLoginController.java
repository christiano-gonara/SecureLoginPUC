package com.example.SecureLoginPUC.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Controller
public class SecureLoginController {

    private final InMemoryUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public SecureLoginController(InMemoryUserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(Model model,
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("cpf") String cpf,
            @RequestParam("rg") String rg,
            @RequestParam("endereco") String endereco,
            @RequestParam("instituicao") String instituicao,
            @RequestParam("senha") String senha,
            @RequestParam("confirmacao") String confirmacao) {

        // Valida e-mail: precisa ter "@" e um "."
        if (!email.contains("@") || !email.contains(".")) {
            model.addAttribute("erro", "E-mail inválido.");
            return "register";
        }

        // Valida senha: tamanho mínimo
        if (senha.length() < 8) {
            model.addAttribute("erro", "A senha deve ter pelo menos 8 caracteres.");
            return "register";
        }

        // Confirma que a senha bate com a confirmação
        if (!senha.equals(confirmacao)) {
            model.addAttribute("erro", "As senhas não coincidem.");
            return "register";
        }

        // Usuário/e-mail já cadastrado?
        if (userDetailsManager.userExists(email)) {
            model.addAttribute("erro", "Este e-mail já está cadastrado.");
            return "register";
        }

        // Salva o usuário no "armazém" com o email como usuário de login
        userDetailsManager.createUser(
            User.withUsername(email)                     // 1
                .password(passwordEncoder.encode(senha)) // 2  BCrypt: texto vira hash
                .roles("USER")                           // 3  perfil padrão
                .build()
        );

        return "redirect:/login";
    }

    @GetMapping("/recoverpassword")
    public String recoverpassword() {
        return "recoverpassword";
    }

    @PostMapping("/recoverpassword")
    public String handleRecoverPassword(Model model, @RequestParam("email") String email) {

        // [VERSÃO DE ESTUDO] simula o envio do link — imprime no terminal do app
        if (userDetailsManager.userExists(email)) {
            System.out.println("[RECUPERAR] Link de redefinição para " + email
                    + " -> http://localhost:8080/recoverpassword?reset=DEMO-" + email);
        }

        // Mensagem genérica de propósito (não revela se o e-mail existe — evita "caça" de contas)
        model.addAttribute("mensagem",
                "Se este e-mail estiver cadastrado, enviaremos um link de redefinição.");
        return "recoverpassword";
    }
}