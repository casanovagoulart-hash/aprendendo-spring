package com.casanova.aprendendospring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @ActiveProfiles("test") faz o Spring carregar application-test.properties
 * (de src/test/resources) por cima do application.properties normal,
 * substituindo a conexão real com PostgreSQL por um banco H2 em memória.
 * Assim este teste sobe o contexto Spring inteiro sem precisar de
 * PostgreSQL rodando nem das variáveis de ambiente DB_PASSWORD/JWT_SECRET -
 * funciona igual no seu terminal, no IntelliJ e no GitHub Actions.
 */
@SpringBootTest
@ActiveProfiles("test")
class AprendendoSpringApplicationTests {

    @Test
    void contextLoads() {
    }
// Exibindo conflitos de codigo. Simulação.
}