package br.com.fiap.techchallenge.functions;

import br.com.fiap.techchallenge.model.RelatorioSemanal;
import br.com.fiap.techchallenge.repository.StorageTableRepository;
import br.com.fiap.techchallenge.service.EmailService;
import br.com.fiap.techchallenge.service.RelatorioService;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RelatorioFunction {

    private static final Logger LOG = Logger.getLogger(RelatorioFunction.class);

    private final RelatorioService relatorioService;
    private final StorageTableRepository repository;
    private final EmailService emailService;

    public RelatorioFunction(RelatorioService relatorioService, StorageTableRepository repository, EmailService emailService) {
        this.relatorioService = relatorioService;
        this.repository = repository;
        this.emailService = emailService;
    }

    @FunctionName("RelatorioSemanalHandler")
    public void run(
            @TimerTrigger(
                    name = "timer",
                    schedule = "0 0 9 * * MON",
                    dataType = "string"
            ) String timerInfo,
            final ExecutionContext context) {

        TelemetryClient telemetryClient = new TelemetryClient();

        LOG.info("=== Iniciando geração de relatório semanal ===");
        LOG.infof("Timer Info: %s", timerInfo);

        try {
            RelatorioSemanal relatorio = relatorioService.gerarRelatorioSemanal();
            LOG.infof("Relatório gerado - ID: %s", relatorio.getId());
            LOG.infof("Total avaliações: %d, Média: %.2f",
                    relatorio.getTotalAvaliacoes(),
                    relatorio.getMediaNotas());

            repository.salvarRelatorio(relatorio);
            LOG.info("Relatório persistido com sucesso");

            emailService.enviarRelatorioSemanal(relatorio);
            LOG.info("E-mail de relatório enviado");

            telemetryClient.trackEvent("RelatorioSemanalGerado");
            telemetryClient.trackMetric("TotalAvaliacoesSemanal", relatorio.getTotalAvaliacoes());
            telemetryClient.trackMetric("MediaNotasSemanal", relatorio.getMediaNotas());

            LOG.info("=== Relatório semanal processado com sucesso ===");

        } catch (Exception e) {
            LOG.errorf("Erro ao gerar relatório semanal: %s", e.getMessage());
            telemetryClient.trackException(e);
            throw new RuntimeException("Falha na geração do relatório semanal", e);
        }
    }
}
