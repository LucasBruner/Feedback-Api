package br.com.fiap.techchallenge.service;

import br.com.fiap.techchallenge.model.RelatorioSemanal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
    }

    @Test
    void testConstruirEmailRelatorio() {
        RelatorioSemanal relatorio = RelatorioSemanal.builder()
                .periodoInicio(LocalDateTime.now().minusDays(7))
                .periodoFim(LocalDateTime.now())
                .dataGeracao(LocalDateTime.now())
                .totalAvaliacoes(100)
                .mediaNotas(8.5)
                .notaMaisAlta(10)
                .notaMaisBaixa(5)
                .avaliacoesPorDia(Map.of("2024-01-01", 50L, "2024-01-02", 50L))
                .contagemPorUrgencia(Map.of("NORMAL", 100L))
                .palavrasRecorrentes(Map.of("bom", 50L, "ótimo", 50L))
                .frasesRecorrentes(Map.of("muito bom", 25L, "atendimento ótimo", 25L))
                .build();

        String emailBody = emailService.construirEmailRelatorio(relatorio);

        assertTrue(emailBody.contains("Relatório Semanal de Feedbacks"));
        assertTrue(emailBody.contains("Total:</strong> 100"));
        assertTrue(emailBody.contains("Média:</strong> 8,50"));
        assertTrue(emailBody.contains("Máxima:</strong> 10"));
        assertTrue(emailBody.contains("Mínima:</strong> 5"));
        assertTrue(emailBody.contains("<strong>NORMAL:</strong> 100"));
        assertTrue(emailBody.contains("bom"));
        assertTrue(emailBody.contains("ótimo"));
        assertTrue(emailBody.contains("muito bom"));
        assertTrue(emailBody.contains("atendimento ótimo"));
    }
}
