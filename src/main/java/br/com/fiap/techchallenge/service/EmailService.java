package br.com.fiap.techchallenge.service;

import br.com.fiap.techchallenge.model.Avaliacao;
import br.com.fiap.techchallenge.model.RelatorioSemanal;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class);
    private static final DateTimeFormatter BRAZIL_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @ConfigProperty(name = "resend.api.key")
    String resendApiKey;

    @ConfigProperty(name = "resend.from.email")
    String fromEmail;

    @ConfigProperty(name = "resend.admin.email")
    String adminEmail;

    public void enviarNotificacaoCritica(Avaliacao avaliacao) {
        try {
            LOG.infof("Enviando notificação crítica para: %s", adminEmail);

            String subject = "⚠️ URGENTE: Nova Avaliação Crítica Recebida";
            String body = construirEmailCritico(avaliacao);

            enviarEmail(adminEmail, subject, body);

            LOG.info("Notificação crítica enviada com sucesso");
        } catch (Exception e) {
            LOG.errorf("Erro ao enviar notificação crítica: %s", e.getMessage());
        }
    }

    public void enviarRelatorioSemanal(RelatorioSemanal relatorio) {
        try {
            LOG.infof("Enviando relatório semanal para: %s", adminEmail);

            String subject = "📊 Relatório Semanal de Feedback";
            String body = construirEmailRelatorio(relatorio);

            enviarEmail(adminEmail, subject, body);

            LOG.info("Relatório semanal enviado com sucesso");
        } catch (Exception e) {
            LOG.errorf("Erro ao enviar relatório semanal: %s", e.getMessage());
        }
    }

    private void enviarEmail(String toEmail, String subject, String body) throws ResendException {
        Resend resend = new Resend(resendApiKey);
        CreateEmailOptions sendEmailRequest = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject(subject)
                .html(body)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(sendEmailRequest);
            LOG.infof("Resend Response - ID: %s", data.getId());
        } catch (ResendException e) {
            LOG.errorf("Erro ao enviar e-mail: %s", e.getMessage());
            throw new ResendException(e.getMessage());
        }
    }

    private String construirEmailCritico(Avaliacao avaliacao) {
        String dataFormatada = avaliacao.getDataHora().format(BRAZIL_FORMATTER);
        return String.format("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background-color: #dc3545; color: white; padding: 20px; border-radius: 5px; }
                                .content { background-color: #f8f9fa; padding: 20px; margin-top: 20px; border-radius: 5px; }
                                .info { margin: 10px 0; }
                                .label { font-weight: bold; }
                                .urgencia { color: #dc3545; font-weight: bold; font-size: 18px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>⚠️ Avaliação Crítica Recebida</h1>
                                </div>
                                <div class="content">
                                    <p>Uma nova avaliação com <span class="urgencia">urgência CRÍTICA</span> foi registrada no sistema.</p>
                        
                                    <div class="info">
                                        <span class="label">ID:</span> %s
                                    </div>
                                    <div class="info">
                                        <span class="label">Data/Hora:</span> %s
                                    </div>
                                    <div class="info">
                                        <span class="label">Nota:</span> %d/10
                                    </div>
                                    <div class="info">
                                        <span class="label">Urgência:</span> %s
                                    </div>
                                    <div class="info">
                                        <span class="label">Descrição:</span>
                                        <p style="background-color: white; padding: 15px; border-left: 4px solid #dc3545; margin-top: 10px;">
                                            %s
                                        </p>
                                    </div>
                        
                                    <p style="margin-top: 20px; color: #666;">
                                        <strong>Ação Recomendada:</strong> Esta avaliação requer atenção imediata.
                                        Por favor, entre em contato com o cliente o mais breve possível.
                                    </p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                avaliacao.getId(),
                dataFormatada,
                avaliacao.getNota(),
                avaliacao.getUrgencia(),
                avaliacao.getDescricao()
        );
    }

    public String construirEmailRelatorio(RelatorioSemanal relatorio) {
        DateTimeFormatter dataHoraFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        DateTimeFormatter apenasDataFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String dataInicioFormatada = relatorio.getPeriodoInicio().format(apenasDataFmt);
        String dataFimFormatada = relatorio.getPeriodoFim().format(apenasDataFmt);
        String dataGeracaoFormatada = relatorio.getDataGeracao().format(dataHoraFmt);

        StringBuilder distribuicaoDiariaHtml = new StringBuilder();
        if (relatorio.getAvaliacoesPorDia() != null && !relatorio.getAvaliacoesPorDia().isEmpty()) {
            distribuicaoDiariaHtml.append("<table style='width:100%; border-collapse: collapse; margin-bottom: 20px;'>");
            distribuicaoDiariaHtml.append("<tr style='background-color: #f2f2f2;'><th style='padding: 8px; border: 1px solid #ddd; text-align: left;'>Data</th><th style='padding: 8px; border: 1px solid #ddd; text-align: center;'>Quantidade</th></tr>");

            relatorio.getAvaliacoesPorDia().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        distribuicaoDiariaHtml.append(String.format(
                                "<tr><td style='padding: 8px; border: 1px solid #ddd;'>%s</td><td style='padding: 8px; border: 1px solid #ddd; text-align: center;'>%d</td></tr>",
                                entry.getKey(), entry.getValue()
                        ));
                    });
            distribuicaoDiariaHtml.append("</table>");
        } else {
            distribuicaoDiariaHtml.append("<p>Nenhuma avaliação registrada no período.</p>");
        }

        String urgencias = relatorio.getAvaliacoesPorUrgencia().entrySet().stream()
                .map(e -> String.format("<li><strong>%s:</strong> %d</li>", e.getKey(), e.getValue()))
                .collect(Collectors.joining());

        String palavrasHtml = relatorio.getPalavrasMaisRecorrentes().stream()
                .map(p -> "<span style='background:#e1f5fe; padding:2px 8px; margin:2px; border-radius:10px; display:inline-block;'>" + p + "</span>")
                .collect(Collectors.joining());

        String frasesHtml = relatorio.getFrasesMaisRecorrentes().stream()
                .map(f -> "<li>\"" + f + "\"</li>")
                .collect(Collectors.joining());

        return String.format("""
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { width: 80%%; margin: 20px auto; border: 1px solid #eee; padding: 20px; border-radius: 8px; }
                .header { background-color: #007bff; color: white; padding: 10px; text-align: center; border-radius: 8px 8px 0 0; }
                .section { margin-top: 20px; padding-bottom: 10px; border-bottom: 1px solid #eee; }
                h2 { color: #007bff; font-size: 18px; }
                .metric-box { display: flex; justify-content: space-between; background: #f8f9fa; padding: 15px; border-radius: 5px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Relatório Semanal de Feedbacks</h1>
                </div>
                <div class="section">
                    <p><strong>Período:</strong> %s até %s</p>
                    <p><strong>Gerado em:</strong> %s</p>
                </div>

                <div class="section">
                    <h2>Métricas de Desempenho</h2>
                    <div class="metric-box">
                        <div><strong>Total:</strong> %d</div>
                        <div><strong>Média:</strong> %.2f</div>
                        <div><strong>Máxima:</strong> %d</div>
                        <div><strong>Mínima:</strong> %d</div>
                    </div>
                </div>

                <div class="section">
                    <h2>Quantidade de Avaliações por Dia</h2>
                    %s
                </div>

                <div class="section">
                    <h2>Distribuição por Urgência</h2>
                    <ul>%s</ul>
                </div>

                <div class="section">
                    <h2>Análise de Texto</h2>
                    <p><strong>Palavras-chave:</strong></p>
                    <div>%s</div>
                    <p><strong>Frases Comuns:</strong></p>
                    <ul>%s</ul>
                </div>

                <div style="font-size: 12px; color: #777; margin-top: 30px; text-align: center;">
                    Sistema Automático de Feedbacks - Tech Challenge Fase 4
                </div>
            </div>
        </body>
        </html>
        """,
                dataInicioFormatada,
                dataFimFormatada,
                dataGeracaoFormatada,
                relatorio.getTotalAvaliacoes(),
                relatorio.getMediaNotas(),
                relatorio.getNotaMaisAlta(),
                relatorio.getNotaMaisBaixa(),
                distribuicaoDiariaHtml.toString(),
                urgencias,
                palavrasHtml,
                frasesHtml
        );
    }
}
