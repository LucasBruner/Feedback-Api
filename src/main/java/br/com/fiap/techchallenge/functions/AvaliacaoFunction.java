package br.com.fiap.techchallenge.functions;

import br.com.fiap.techchallenge.model.Avaliacao;
import br.com.fiap.techchallenge.repository.AvaliacaoRepository;
import br.com.fiap.techchallenge.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.jboss.logging.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AvaliacaoFunction {

    private static final Logger LOG = Logger.getLogger(AvaliacaoFunction.class);
    private static final int URGENCIA_CRITICA_THRESHOLD = 3;

    private final AvaliacaoRepository avaliacaoRepository;
    private final EmailService emailService;
    private final Validator validator;
    private final ObjectMapper objectMapper;

    public AvaliacaoFunction(AvaliacaoRepository avaliacaoRepository, EmailService emailService, Validator validator) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.emailService = emailService;
        this.validator = validator;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @FunctionName("AvaliacaoHandler")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION,
                    route = "avaliacao"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        TelemetryClient telemetryClient = new TelemetryClient();

        LOG.info("=== Iniciando processamento de avaliação ===");

        try {
            String body = request.getBody().orElse(null);
            if (body == null || body.isBlank()) {
                LOG.warn("Body vazio recebido");
                return criarRespostaErro(request, 400, "Body da requisição é obrigatório");
            }

            Avaliacao avaliacao = objectMapper.readValue(body, Avaliacao.class);
            LOG.infof("Avaliação parseada - Nota: %d", avaliacao.getNota());

            Set<ConstraintViolation<Avaliacao>> violations = validator.validate(avaliacao);
            if (!violations.isEmpty()) {
                String erros = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                LOG.warnf("Validação falhou: %s", erros);
                return criarRespostaErro(request, 400, "Erro de validação: " + erros);
            }

            avaliacao.inicializar();
            avaliacao.calcularUrgencia(URGENCIA_CRITICA_THRESHOLD);
            LOG.infof("Urgência calculada: %s", avaliacao.getUrgencia());

            avaliacaoRepository.salvar(avaliacao);
            LOG.info("Avaliação persistida com sucesso");

            if (avaliacao.getUrgencia() == Avaliacao.NivelUrgencia.CRITICO) {
                LOG.warn("Avaliação CRÍTICA detectada - enviando notificação");
                emailService.enviarNotificacaoCritica(avaliacao);
                telemetryClient.trackEvent("AvaliacaoCritica");
            }

            telemetryClient.trackEvent("AvaliacaoRecebida");
            telemetryClient.trackMetric("NotaAvaliacao", avaliacao.getNota());

            LOG.info("=== Avaliação processada com sucesso ===");

            String avaliacaoJson = objectMapper.writeValueAsString(avaliacao);

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(avaliacaoJson) // passa String, não o objeto
                    .build();

        } catch (Exception e) {
            LOG.errorf("Erro ao processar avaliação: %s", e.getMessage());
            telemetryClient.trackException(e);
            return criarRespostaErro(request, 500, "Erro interno: " + e.getMessage());
        }
    }

    private HttpResponseMessage criarRespostaErro(HttpRequestMessage<?> request, int status, String mensagem) {
        String json = String.format("{\"erro\": \"%s\"}", mensagem);
        return request.createResponseBuilder(HttpStatus.valueOf(status))
                .header("Content-Type", "application/json")
                .body(json)
                .build();
    }
}
