package com.casanova.aprendendospring.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/* @Configuration: Pertence Spring Framework.
Indica que a classe contém definições de beans e configurações.
É como uma “classe de receitas” que o Spring lê para saber quais objetos criar e como configurar. */
@Configuration // indica que esta classe contém configurações do Spring
/* @EnableWebSecurity : Pertence ao Spring Security.
Ativa as funcionalidades de segurança na aplicação.
É como ligar a “central de segurança” do Spring, permitindo configurar autenticação, autorização e filtros. */
@EnableWebSecurity // habilita o Spring Security na aplicação
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /* @Autowired: Pertence ao Spring Framework.
Serve para injeção de dependência automática: o Spring procura um objeto (bean) compatível e injeta na classe.
É como pedir ao Spring: “traga para mim o componente certo já pronto”. */
    @Autowired // Injeta automaticamente um bean já existente
    public SecurityConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /* @Bean: Pertence ao Spring Framework.
Marca um método dentro de uma classe de configuração para que o retorno seja registrado como um bean no contexto da aplicação.
É como dizer: “este método fabrica um objeto que deve ficar disponível para todos”. */
    @Bean // Cria e registra um novo bean no contexto
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Filtro que intercepta requisições e valida o token JWT
        JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(jwtUtil, userDetailsService);

        http
                .cors(cors -> {}) // habilita suporte a CORS (Cross-Origin Resource Sharing)
                .csrf(AbstractHttpConfigurer::disable) /* Desabilita CSRF pois estamos usando JWT (não há sessão tradicional).
                Desabilita proteção csrf para apis rest. Para desabilitar a proteção CSRF em APIs REST,
                você pode usar o método " csrf.disable() " na configuração do Spring Security. Isso é especialmente útil quando a aplicação utiliza tokens JWT,
                pois a proteção CSRF depende de cookies, e isso não é o caso em APIs REST. O Spring Security, por padrão,
                habilita a proteção CSRF para evitar ataques CSRF em aplicações web com sessões e formulários HTML.
No              entanto, pode ser desativado em APIs REST que não suportam cookies, como as que utilizam tokens JWT */
                .authorizeHttpRequests(authorize -> authorize
                        // libera requisições OPTIONS (necessárias para preflight em CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // libera login sem autenticação
                        .requestMatchers("/usuarios/login").permitAll()
                        // libera endpoint de teste de autenticação
                        .requestMatchers(HttpMethod.GET, "/auth").permitAll()
                        // libera criação de usuário (cadastro)
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                        // libera acesso à documentação Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()
                        // exige autenticação para qualquer endpoint de usuário
                        .requestMatchers("/usuarios/**").authenticated()
                        // qualquer outra requisição precisa estar autenticada
                        .anyRequest().authenticated()
                )
                // define que não haverá sessão no servidor (stateless), pois JWT carrega as informações
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // adiciona o filtro JWT antes do filtro padrão de autenticação por usuário/senha
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt é um algoritmo forte para criptografar senhas
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // Gerencia autenticação, integrando com o UserDetailsService
        return authenticationConfiguration.getAuthenticationManager();
    }
}
