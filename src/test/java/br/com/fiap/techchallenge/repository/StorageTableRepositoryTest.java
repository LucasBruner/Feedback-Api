package br.com.fiap.techchallenge.repository;

import br.com.fiap.techchallenge.model.Avaliacao;
import br.com.fiap.techchallenge.model.RelatorioSemanal;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.models.TableEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageTableRepositoryTest {

    @Mock
    private TableServiceClient tableServiceClient;

    @Mock
    private TableClient tableClient;

    @InjectMocks
    private StorageTableRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        // Mock the environment variable
        System.setProperty("AZURE_STORAGE_CONNECTION_STRING", "UseDevelopmentStorage=true");

        // Inject the mocked TableServiceClient
        Field field = StorageTableRepository.class.getDeclaredField("tableServiceClient");
        field.setAccessible(true);
        field.set(repository, tableServiceClient);

        when(tableServiceClient.getTableClient(anyString())).thenReturn(tableClient);
    }

    @Test
    void testSalvarAvaliacao() {
        Avaliacao avaliacao = Avaliacao.builder()
                .id("test-id")
                .descricao("ótima")
                .nota(10)
                .urgencia(Avaliacao.NivelUrgencia.NORMAL)
                .dataHora(LocalDateTime.now())
                .build();

        repository.salvarAvaliacao(avaliacao);

        ArgumentCaptor<TableEntity> captor = ArgumentCaptor.forClass(TableEntity.class);
        verify(tableClient, times(1)).createEntity(captor.capture());

        TableEntity entity = captor.getValue();
        assertEquals("test-id", entity.getRowKey());
        assertEquals("ótima", entity.getProperty("descricao"));
        assertEquals(10, entity.getProperty("nota"));
    }

    @Test
    void testSalvarRelatorio() {
        RelatorioSemanal relatorio = RelatorioSemanal.builder()
                .id("relatorio-id")
                .totalAvaliacoes(50)
                .mediaNotas(7.5)
                .build();
        relatorio.inicializar();

        repository.salvarRelatorio(relatorio);

        ArgumentCaptor<TableEntity> captor = ArgumentCaptor.forClass(TableEntity.class);
        verify(tableClient, times(1)).createEntity(captor.capture());

        TableEntity entity = captor.getValue();
        assertEquals("relatorio-id", entity.getRowKey());
        assertEquals(50L, entity.getProperty("totalAvaliacoes"));
        assertEquals(7.5, entity.getProperty("mediaNotas"));
    }
}
