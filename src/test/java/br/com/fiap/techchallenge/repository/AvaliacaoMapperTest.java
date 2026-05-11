package br.com.fiap.techchallenge.repository;

import br.com.fiap.techchallenge.model.Avaliacao;
import com.azure.data.tables.models.TableEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AvaliacaoMapperTest {

    private static final String PARTITION_KEY = "TestPartition";

    @Test
    void shouldConvertToTableEntity() {
        Avaliacao avaliacao = Avaliacao.builder()
                .id("test-id")
                .descricao("Excelente")
                .nota(10)
                .urgencia(Avaliacao.NivelUrgencia.NORMAL)
                .dataHora(LocalDateTime.parse("2024-01-01T10:00:00"))
                .build();

        TableEntity entity = AvaliacaoMapper.toTableEntity(avaliacao, PARTITION_KEY);

        assertEquals(PARTITION_KEY, entity.getPartitionKey());
        assertEquals("test-id", entity.getRowKey());
        assertEquals("Excelente", entity.getProperty("descricao"));
        assertEquals(10, entity.getProperty("nota"));
        assertEquals("NORMAL", entity.getProperty("urgencia"));
    }

    @Test
    void shouldConvertFromTableEntity() {
        TableEntity entity = new TableEntity(PARTITION_KEY, "test-id-2")
                .addProperty("descricao", "Ruim")
                .addProperty("nota", 2)
                .addProperty("urgencia", "CRITICO")
                .addProperty("dataHora", "2024-01-02T12:30:00");

        Avaliacao avaliacao = AvaliacaoMapper.fromTableEntity(entity);

        assertEquals("test-id-2", avaliacao.getId());
        assertEquals("Ruim", avaliacao.getDescricao());
        assertEquals(2, avaliacao.getNota());
        assertEquals(Avaliacao.NivelUrgencia.CRITICO, avaliacao.getUrgencia());
        assertEquals(LocalDateTime.parse("2024-01-02T12:30:00"), avaliacao.getDataHora());
    }

    @Test
    void shouldHandleNullValuesWhenConvertingFromTableEntity() {
        TableEntity entity = new TableEntity(PARTITION_KEY, "test-id-3");

        Avaliacao avaliacao = AvaliacaoMapper.fromTableEntity(entity);

        assertNotNull(avaliacao);
        assertEquals("test-id-3", avaliacao.getId());
        assertEquals("", avaliacao.getDescricao());
        assertEquals(0, avaliacao.getNota());
        assertEquals(Avaliacao.NivelUrgencia.NORMAL, avaliacao.getUrgencia());
        assertNotNull(avaliacao.getDataHora());
    }
}
