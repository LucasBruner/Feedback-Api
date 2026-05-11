package br.com.fiap.techchallenge.repository;

import br.com.fiap.techchallenge.exception.SalvarAvaliacaoException;
import br.com.fiap.techchallenge.exception.SalvarRelatorioException;
import br.com.fiap.techchallenge.model.Avaliacao;
import br.com.fiap.techchallenge.model.RelatorioSemanal;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class StorageTableRepository {

    private static final Logger LOG = Logger.getLogger(StorageTableRepository.class);
    private static final String TABLE_AVALIACOES = "avaliacoes";
    private static final String TABLE_RELATORIOS = "relatorios";
    private static final String PARTITION_KEY_AVALIACOES = "Java";
    private static final String PARTITION_KEY_RELATORIOS = "Semanal";
    private static final String FORMAT_STRING = "\"%s\":%d";

    private final TableServiceClient tableServiceClient;

    public StorageTableRepository() {
        LOG.info("Inicializando conexão com Azure Storage Tables");
        String connectionString = System.getenv("AzureWebJobsStorage");
        if (connectionString == null || connectionString.isBlank()) {
            connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        }
        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException("Connection string do Azure Storage não configurada. Defina AzureWebJobsStorage ou AZURE_STORAGE_CONNECTION_STRING.");
        }
        this.tableServiceClient = new TableServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        LOG.info("Conexão estabelecida com sucesso. Verificando tabelas...");
        criarTabelaAvaliacoes();
        criarTabelaRelatorios();
    }

    public void salvarAvaliacao(Avaliacao avaliacao) {
        try {
            LOG.infof("Salvando avaliação: %s", avaliacao.getId());

            TableClient tableClient = tableServiceClient.getTableClient(TABLE_AVALIACOES);
            TableEntity entity = new TableEntity(PARTITION_KEY_AVALIACOES, avaliacao.getId())
                    .addProperty("descricao", avaliacao.getDescricao())
                    .addProperty("nota", avaliacao.getNota())
                    .addProperty("urgencia", avaliacao.getUrgencia() != null ? avaliacao.getUrgencia().toString() : "NORMAL")
                    .addProperty("dataHora", avaliacao.getDataHora() != null ? avaliacao.getDataHora().toString() : LocalDateTime.now().toString());

            tableClient.createEntity(entity);
            LOG.infof("Avaliação salva com sucesso: %s", avaliacao.getId());
        } catch (Exception e) {
            LOG.errorf("Erro ao salvar avaliação: %s", e.getMessage());
            throw new SalvarAvaliacaoException("Erro ao salvar avaliação", e);
        }
    }

    private void criarTabelaAvaliacoes() {
        try {
            tableServiceClient.createTableIfNotExists(TABLE_AVALIACOES);
        } catch (Exception e) {
            LOG.warnf("Tabela %s pode já existir ou erro ao criar: %s", TABLE_AVALIACOES, e.getMessage());
        }
    }

    public List<Avaliacao> buscarAvaliacoesPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try {
            LOG.infof("Buscando avaliações entre %s e %s", inicio, fim);

            TableClient tableClient = tableServiceClient.getTableClient(TABLE_AVALIACOES);

            String filter = String.format("Timestamp ge datetime'%s' and Timestamp lt datetime'%s'",
                    inicio.format(DateTimeFormatter.ISO_DATE_TIME),
                    fim.format(DateTimeFormatter.ISO_DATE_TIME));

            ListEntitiesOptions options = new ListEntitiesOptions().setFilter(filter);

            List<Avaliacao> avaliacoes = tableClient.listEntities(options, null, null)
                    .stream()
                    .map(this::fromTableEntity)
                    .toList();

            LOG.infof("Total de avaliações encontradas: %d", avaliacoes.size());
            return avaliacoes;
        } catch (Exception e) {
            LOG.errorf("Erro ao buscar avaliações: %s", e.getMessage());
            return new ArrayList<>();
        }
    }

    public void salvarRelatorio(RelatorioSemanal relatorio) {
        try {
            LOG.infof("Salvando relatório: %s", relatorio.getId());

            TableEntity entity = criarEntityBase(relatorio);
            adicionarAvaliacoesPorDia(entity, relatorio.getAvaliacoesPorDia());
            adicionarContagemPorUrgencia(entity, relatorio.getContagemPorUrgencia());
            adicionarMapaComoJson(entity, "palavrasRecorrentes", relatorio.getPalavrasRecorrentes());
            adicionarMapaComoJson(entity, "frasesRecorrentes", relatorio.getFrasesRecorrentes());

            tableServiceClient.getTableClient(TABLE_RELATORIOS).createEntity(entity);
            LOG.infof("Relatório salvo com sucesso: %s", relatorio.getId());

        } catch (Exception e) {
            LOG.errorf("Erro ao salvar relatório: %s", e.getMessage());
            throw new SalvarRelatorioException("Erro ao salvar relatório", e);
        }
    }

    private TableEntity criarEntityBase(RelatorioSemanal relatorio) {
        String dataGeracao = relatorio.getDataGeracao() != null
                ? relatorio.getDataGeracao().toString()
                : LocalDateTime.now().toString();

        String periodoInicio = relatorio.getPeriodoInicio() != null
                ? relatorio.getPeriodoInicio().toString()
                : "";

        String periodoFim = relatorio.getPeriodoFim() != null
                ? relatorio.getPeriodoFim().toString()
                : "";

        return new TableEntity(PARTITION_KEY_RELATORIOS, relatorio.getId())
                .addProperty("dataGeracao", dataGeracao)
                .addProperty("periodoInicio", periodoInicio)
                .addProperty("periodoFim", periodoFim)
                .addProperty("totalAvaliacoes", relatorio.getTotalAvaliacoes())
                .addProperty("mediaNotas", relatorio.getMediaNotas())
                .addProperty("notaMaisAlta", Objects.requireNonNullElse(relatorio.getNotaMaisAlta(), 0))
                .addProperty("notaMaisBaixa", Objects.requireNonNullElse(relatorio.getNotaMaisBaixa(), 0));
    }

    private void adicionarAvaliacoesPorDia(TableEntity entity,
                                           Map<String, Long> avaliacoesPorDia) {
        if (avaliacoesPorDia == null) return;

        String json = serializarMapaParaJson(
                avaliacoesPorDia,
                dia -> dia,
                String::valueOf
        );

        entity.addProperty("avaliacoesPorDia", json);
    }

    private void adicionarContagemPorUrgencia(TableEntity entity,
                                              Map<String, Long> contagemPorUrgencia) {
        if (contagemPorUrgencia == null) return;

        contagemPorUrgencia.forEach((nivel, count) ->
                entity.addProperty("contagem_" + nivel, count));
    }

    private void adicionarMapaComoJson(TableEntity entity, String propriedade, Map<String, Long> mapa) {
        if (mapa == null || mapa.isEmpty()) return;

        String json = serializarMapaParaJson(
                mapa,
                chave -> sanitizarParaJson(Objects.requireNonNullElse(chave, "")),
                valor -> String.valueOf(Objects.requireNonNullElse(valor, 0L))
        );

        entity.addProperty(propriedade, json);
    }

    private <K, V> String serializarMapaParaJson(
            Map<K, V> mapa,
            Function<K, String> serializarChave,
            Function<V, String> serializarValor) {

        String conteudo = mapa.entrySet().stream()
                .map(entry -> String.format(FORMAT_STRING,
                        serializarChave.apply(entry.getKey()),
                        serializarValor.apply(entry.getValue())))
                .collect(Collectors.joining(","));

        return "{" + conteudo + "}";
    }

    private String sanitizarParaJson(String valor) {
        return valor.replace("\"", "\\\"");
    }

    private void criarTabelaRelatorios() {
        try {
            tableServiceClient.createTableIfNotExists(TABLE_RELATORIOS);
        } catch (Exception e) {
            LOG.warnf("Tabela %s pode já existir ou erro ao criar: %s", TABLE_RELATORIOS, e.getMessage());
        }
    }

    private Avaliacao fromTableEntity(TableEntity entity) {
        String descricao = (String) entity.getProperty("descricao");
        Integer nota = (Integer) entity.getProperty("nota");
        String urgenciaStr = (String) entity.getProperty("urgencia");
        String dataHoraStr = (String) entity.getProperty("dataHora");

        Avaliacao.NivelUrgencia urgencia = Avaliacao.NivelUrgencia.NORMAL;
        if (urgenciaStr != null) {
            try {
                urgencia = Avaliacao.NivelUrgencia.valueOf(urgenciaStr);
            } catch (IllegalArgumentException e) {
                LOG.warnf("Urgência inválida: %s", urgenciaStr);
            }
        }

        LocalDateTime dataHora = null;
        if (dataHoraStr != null) {
            try {
                dataHora = LocalDateTime.parse(dataHoraStr);
            } catch (Exception e) {
                LOG.warnf("Data inválida: %s", dataHoraStr);
                dataHora = LocalDateTime.now();
            }
        } else {
            dataHora = LocalDateTime.now();
        }

        return Avaliacao.builder()
                .id(entity.getRowKey())
                .descricao(descricao != null ? descricao : "")
                .nota(nota != null ? nota : 0)
                .urgencia(urgencia)
                .dataHora(dataHora)
                .build();
    }
}
