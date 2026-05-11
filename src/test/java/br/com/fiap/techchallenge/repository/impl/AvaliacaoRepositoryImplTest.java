package br.com.fiap.techchallenge.repository.impl;

import br.com.fiap.techchallenge.model.Avaliacao;
import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoRepositoryImplTest {

    @Mock
    private TableServiceClient tableServiceClient;
    @Mock
    private TableClient tableClient;
    @Mock
    private PagedIterable<TableEntity> pagedIterable;

    private AvaliacaoRepositoryImpl avaliacaoRepository;

    @BeforeEach
    void setUp() {
        when(tableServiceClient.getTableClient("avaliacoes")).thenReturn(tableClient);
        avaliacaoRepository = new AvaliacaoRepositoryImpl(tableServiceClient);
    }

    @Test
    void shouldSalvarAvaliacao() {
        Avaliacao avaliacao = Avaliacao.builder()
                .id("test-id")
                .descricao("Teste")
                .nota(5)
                .build();

        avaliacaoRepository.salvar(avaliacao);

        ArgumentCaptor<TableEntity> captor = ArgumentCaptor.forClass(TableEntity.class);
        verify(tableClient, times(1)).createEntity(captor.capture());
        TableEntity capturedEntity = captor.getValue();

        assertEquals("Java", capturedEntity.getPartitionKey());
        assertEquals("test-id", capturedEntity.getRowKey());
        assertEquals("Teste", capturedEntity.getProperty("descricao"));
    }

    @Test
    void shouldBuscarAvaliacoesPorPeriodo() {
        TableEntity entity1 = new TableEntity("Java", "id1").addProperty("nota", 10);
        TableEntity entity2 = new TableEntity("Java", "id2").addProperty("nota", 8);

        when(tableClient.listEntities(any(ListEntitiesOptions.class), any(), any())).thenReturn(pagedIterable);
        when(pagedIterable.stream()).thenReturn(Stream.of(entity1, entity2));

        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now();
        List<Avaliacao> result = avaliacaoRepository.buscarPorPeriodo(inicio, fim);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(10, result.get(0).getNota());
    }
}
