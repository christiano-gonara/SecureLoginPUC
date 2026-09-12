package com.example.SecureLoginPUC.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Dependência entregue por constructor injection (Spring injeta automaticamente)
    private final UserConfig userConfig;

    public SecurityConfig(UserConfig userConfig) {
        this.userConfig = userConfig;
    }

    // Define as regras de acesso de TODA a aplicação
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Páginas e recursos LIBERADOS (sem precisar de login)
                        .requestMatchers(HttpMethod.GET, "/login/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/css/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/recoverpassword").permitAll()
                        .requestMatchers(HttpMethod.POST, "/recoverpassword").permitAll()
                        .requestMatchers(HttpMethod.GET, "/error").permitAll()

                        // /admin/** só acessível por quem tem a ROLE ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Qualquer outra rota exige usuário autenticado
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // página de login personalizada
                        .permitAll()
                        // Depois do login, cada perfil vai para sua página
                        .successHandler((request, response, authentication) -> {
                            if (authentication.getAuthorities().stream()
                                    .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"))) {
                                response.sendRedirect("/admin");
                            } else {
                                response.sendRedirect("/home");
                            }
                        })
                        // Falha no login -> página de erro
                        .failureHandler((request, response, authentication) -> {
                            response.sendRedirect("/error");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll());
        return http.build();
    }

    // Os 2 usuários iniciais (vêm das configs no application.properties)
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.builder()
                .username(userConfig.getUserUsername())
                .password(passwordEncoder().encode(userConfig.getUserPassword()))
                .roles("USER")  // perfil comum
                .build();
        UserDetails admin = User.builder()
                .username(userConfig.getAdminUsername())
                .password(passwordEncoder().encode(userConfig.getAdminPassword()))
                .roles("ADMIN") // perfil administrador
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    // Camada hash das senhas (nunca guarda em texto puro)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}