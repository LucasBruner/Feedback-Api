package br.com.fiap.techchallenge.service;

import br.com.fiap.techchallenge.model.Avaliacao;
import br.com.fiap.techchallenge.model.RelatorioSemanal;
import br.com.fiap.techchallenge.repository.StorageTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private StorageTableRepository repository;

    @Mock
    private AnaliseTextoService analiseTextoService;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void testGerarRelatorioSemanalComAvaliacoes() {
        Avaliacao a1 = Avaliacao.builder().nota(8).urgencia(Avaliacao.NivelUrgencia.NORMAL).descricao("bom").dataHora(LocalDateTime.now()).build();
        Avaliacao a2 = Avaliacao.builder().nota(4).urgencia(Avaliacao.NivelUrgencia.ALTO).descricao("ruim").dataHora(LocalDateTime.now()).build();
        when(repository.buscarAvaliacoesPorPeriodo(any(), any())).thenReturn(List.of(a1, a2));
        when(analiseTextoService.analisarPalavrasRecorrentes(any())).thenReturn(Map.of("bom", 1L, "ruim", 1L));
        when(analiseTextoService.analisarFrasesRecorrentes(any())).thenReturn(Map.of());

        RelatorioSemanal relatorio = relatorioService.gerarRelatorioSemanal();

        assertEquals(2, relatorio.getTotalAvaliacoes());
        assertEquals(6.0, relatorio.getMediaNotas());
        assertEquals(8, relatorio.getNotaMaisAlta());
        assertEquals(4, relatorio.getNotaMaisBaixa());
        assertEquals(1, relatorio.getContagemPorUrgencia().get("NORMAL"));
        assertEquals(1, relatorio.getContagemPorUrgencia().get("ALTO"));
    }

    @Test
    void testGerarRelatorioSemanalSemAvaliacoes() {
        when(repository.buscarAvaliacoesPorPeriodo(any(), any())).thenReturn(Collections.emptyList());

        RelatorioSemanal relatorio = relatorioService.gerarRelatorioSemanal();

        assertEquals(0, relatorio.getTotalAvaliacoes());
        assertEquals(0.0, relatorio.getMediaNotas());
    }
}
