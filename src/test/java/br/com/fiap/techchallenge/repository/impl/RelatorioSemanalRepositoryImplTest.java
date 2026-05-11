package br.com.fiap.techchallenge.repository.impl;

import br.com.fiap.techchallenge.model.RelatorioSemanal;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.models.TableEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioSemanalRepositoryImplTest {

    @Mock
    private TableServiceClient tableServiceClient;
    @Mock
    private TableClient tableClient;

    private RelatorioSemanalRepositoryImpl relatorioRepository;

    @BeforeEach
    void setUp() {
        when(tableServiceClient.getTableClient("relatorios")).thenReturn(tableClient);
        relatorioRepository = new RelatorioSemanalRepositoryImpl(tableServiceClient);
    }

    @Test
    void shouldSalvarRelatorio() {
        RelatorioSemanal relatorio = RelatorioSemanal.builder()
                .id("relatorio-id")
                .totalAvaliacoes(50L)
                .mediaNotas(7.8)
                .build();

        relatorioRepository.salvar(relatorio);

        ArgumentCaptor<TableEntity> captor = ArgumentCaptor.forClass(TableEntity.class);
        verify(tableClient, times(1)).createEntity(captor.capture());
        TableEntity capturedEntity = captor.getValue();

        assertEquals("Semanal", capturedEntity.getPartitionKey());
        assertEquals("relatorio-id", capturedEntity.getRowKey());
        assertEquals(50L, capturedEntity.getProperty("totalAvaliacoes"));
        assertEquals(7.8, capturedEntity.getProperty("mediaNotas"));
    }
}
