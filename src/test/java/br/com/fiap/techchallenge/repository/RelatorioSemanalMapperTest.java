package br.com.fiap.techchallenge.repository;

import br.com.fiap.techchallenge.model.RelatorioSemanal;
import com.azure.data.tables.models.TableEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RelatorioSemanalMapperTest {

    private static final String PARTITION_KEY = "TestPartition";

    @Test
    void shouldConvertToTableEntity() {
        RelatorioSemanal relatorio = RelatorioSemanal.builder()
                .id("relatorio-1")
                .dataGeracao(LocalDateTime.parse("2024-01-10T11:00:00"))
                .periodoInicio(LocalDateTime.parse("2024-01-01T00:00:00"))
                .periodoFim(LocalDateTime.parse("2024-01-08T00:00:00"))
                .totalAvaliacoes(150L)
                .mediaNotas(8.5)
                .notaMaisAlta(10)
                .notaMaisBaixa(3)
                .contagemPorUrgencia(Map.of("NORMAL", 100L, "ALTO", 50L))
                .palavrasRecorrentes(Map.of("bom", 80L, "ruim", 20L))
                .frasesRecorrentes(Map.of("muito bom", 10L))
                .avaliacoesPorDia(Map.of("2024-01-01", 20L, "2024-01-02", 25L))
                .build();

        TableEntity entity = RelatorioSemanalMapper.toTableEntity(relatorio, PARTITION_KEY);

        assertEquals(PARTITION_KEY, entity.getPartitionKey());
        assertEquals("relatorio-1", entity.getRowKey());
        assertEquals(150L, entity.getProperty("totalAvaliacoes"));
        assertEquals(8.5, entity.getProperty("mediaNotas"));
        assertEquals(100L, entity.getProperty("contagem_NORMAL"));
        assertNotNull(entity.getProperty("palavrasRecorrentes"));
        assertNotNull(entity.getProperty("avaliacoesPorDia"));
    }
}
