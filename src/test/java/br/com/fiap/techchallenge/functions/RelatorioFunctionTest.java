package br.com.fiap.techchallenge.functions;

import br.com.fiap.techchallenge.model.RelatorioSemanal;
import br.com.fiap.techchallenge.repository.StorageTableRepository;
import br.com.fiap.techchallenge.service.EmailService;
import br.com.fiap.techchallenge.service.RelatorioService;
import com.microsoft.azure.functions.ExecutionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioFunctionTest {

    @Mock
    private RelatorioService relatorioService;

    @Mock
    private StorageTableRepository repository;

    @Mock
    private EmailService emailService;

    @Mock
    private ExecutionContext context;

    @InjectMocks
    private RelatorioFunction relatorioFunction;

    @Test
    void testRun() {
        RelatorioSemanal relatorio = RelatorioSemanal.builder().id("test-id").build();
        when(relatorioService.gerarRelatorioSemanal()).thenReturn(relatorio);

        relatorioFunction.run("timer-info", context);

        verify(relatorioService, times(1)).gerarRelatorioSemanal();
        verify(repository, times(1)).salvarRelatorio(relatorio);
        verify(emailService, times(1)).enviarRelatorioSemanal(relatorio);
    }
}
