package br.com.fiap.techchallenge.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AvaliacaoTest {

    @Test
    void testInicializar() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.inicializar();
        assertNotNull(avaliacao.getId());
        assertNotNull(avaliacao.getDataHora());
    }

    @Test
    void testInicializarComDados() {
        String id = "test-id";
        LocalDateTime data = LocalDateTime.now().minusDays(1);
        Avaliacao avaliacao = Avaliacao.builder().id(id).dataHora(data).build();
        avaliacao.inicializar();
        assertEquals(id, avaliacao.getId());
        assertEquals(data, avaliacao.getDataHora());
    }

    @Test
    void testCalcularUrgenciaCritico() {
        Avaliacao avaliacao = Avaliacao.builder().nota(3).build();
        avaliacao.calcularUrgencia(4);
        assertEquals(Avaliacao.NivelUrgencia.CRITICO, avaliacao.getUrgencia());
    }

    @Test
    void testCalcularUrgenciaAlto() {
        Avaliacao avaliacao = Avaliacao.builder().nota(5).build();
        avaliacao.calcularUrgencia(4);
        assertEquals(Avaliacao.NivelUrgencia.ALTO, avaliacao.getUrgencia());
    }

    @Test
    void testCalcularUrgenciaNormal() {
        Avaliacao avaliacao = Avaliacao.builder().nota(7).build();
        avaliacao.calcularUrgencia(4);
        assertEquals(Avaliacao.NivelUrgencia.NORMAL, avaliacao.getUrgencia());
    }
}
