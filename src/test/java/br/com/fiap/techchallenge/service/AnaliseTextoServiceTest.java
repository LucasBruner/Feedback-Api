package br.com.fiap.techchallenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnaliseTextoServiceTest {

    private AnaliseTextoService analiseTextoService;

    @BeforeEach
    void setUp() {
        analiseTextoService = new AnaliseTextoService();
    }

    @Test
    void testAnalisarPalavrasRecorrentes() {
        List<String> descricoes = List.of(
                "O atendimento foi ótimo, muito bom mesmo.",
                "Gostei do atendimento, foi rápido e eficiente.",
                "Atendimento péssimo, não recomendo."
        );

        Map<String, Long> palavras = analiseTextoService.analisarPalavrasRecorrentes(descricoes);

        assertEquals(Long.valueOf(3), palavras.get("atendimento"));
        assertTrue(palavras.containsKey("ótimo"));
        assertTrue(palavras.containsKey("bom"));
        assertTrue(palavras.containsKey("rápido"));
        assertTrue(palavras.containsKey("eficiente"));
        assertTrue(palavras.containsKey("péssimo"));
    }

    @Test
    void testAnalisarFrasesRecorrentes() {
        List<String> descricoes = List.of(
                "O atendimento foi ótimo, muito bom mesmo.",
                "Gostei do atendimento, foi rápido e eficiente.",
                "Atendimento péssimo, não recomendo.",
                "O atendimento foi ótimo, gostei bastante."
        );

        Map<String, Long> frases = analiseTextoService.analisarFrasesRecorrentes(descricoes);

        assertEquals(Long.valueOf(2), frases.get("atendimento ótimo"));
    }
}
