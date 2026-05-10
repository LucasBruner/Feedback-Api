package br.com.fiap.techchallenge.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioSemanalTest {

    @Test
    void testInicializar() {
        RelatorioSemanal relatorio = new RelatorioSemanal();
        relatorio.inicializar();
        assertNotNull(relatorio.getId());
        assertNotNull(relatorio.getDataGeracao());
        assertNotNull(relatorio.getPalavrasRecorrentes());
        assertNotNull(relatorio.getFrasesRecorrentes());
        assertNotNull(relatorio.getAvaliacoesPorDia());
    }

    @Test
    void testGetPalavrasMaisRecorrentes() {
        Map<String, Long> palavras = Map.of("boa", 10L, "ruim", 5L);
        RelatorioSemanal relatorio = RelatorioSemanal.builder().palavrasRecorrentes(palavras).build();
        List<String> palavrasMaisRecorrentes = relatorio.getPalavrasMaisRecorrentes();
        assertTrue(palavrasMaisRecorrentes.contains("boa"));
        assertTrue(palavrasMaisRecorrentes.contains("ruim"));
    }

    @Test
    void testGetFrasesMaisRecorrentes() {
        Map<String, Long> frases = Map.of("muito bom", 10L, "pode melhorar", 5L);
        RelatorioSemanal relatorio = RelatorioSemanal.builder().frasesRecorrentes(frases).build();
        List<String> frasesMaisRecorrentes = relatorio.getFrasesMaisRecorrentes();
        assertTrue(frasesMaisRecorrentes.contains("muito bom"));
        assertTrue(frasesMaisRecorrentes.contains("pode melhorar"));
    }
}
