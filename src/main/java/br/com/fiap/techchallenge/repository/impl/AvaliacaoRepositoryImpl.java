package br.com.fiap.techchallenge.repository.impl;

import br.com.fiap.techchallenge.exception.SalvarAvaliacaoException;
import br.com.fiap.techchallenge.model.Avaliacao;
import br.com.fiap.techchallenge.repository.AvaliacaoMapper;
import br.com.fiap.techchallenge.repository.AvaliacaoRepository;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AvaliacaoRepositoryImpl implements AvaliacaoRepository {

    private static final Logger LOG = Logger.getLogger(AvaliacaoRepositoryImpl.class);
    private static final String TABLE_AVALIACOES = "avaliacoes";
    private static final String PARTITION_KEY_AVALIACOES = "Java";

    private final TableServiceClient tableServiceClient;

    @Inject
    public AvaliacaoRepositoryImpl(TableServiceClient tableServiceClient) {
        this.tableServiceClient = tableServiceClient;
        criarTabelaAvaliacoes();
    }

    @Override
    public void salvar(Avaliacao avaliacao) {
        try {
            LOG.infof("Salvando avaliação: %s", avaliacao.getId());

            TableClient tableClient = tableServiceClient.getTableClient(TABLE_AVALIACOES);
            TableEntity entity = AvaliacaoMapper.toTableEntity(avaliacao, PARTITION_KEY_AVALIACOES);

            tableClient.createEntity(entity);
            LOG.infof("Avaliação salva com sucesso: %s", avaliacao.getId());
        } catch (Exception e) {
            LOG.errorf("Erro ao salvar avaliação: %s", e.getMessage());
            throw new SalvarAvaliacaoException("Erro ao salvar avaliação", e);
        }
    }

    @Override
    public List<Avaliacao> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        try {
            LOG.infof("Buscando avaliações entre %s e %s", inicio, fim);

            TableClient tableClient = tableServiceClient.getTableClient(TABLE_AVALIACOES);

            String filter = String.format("Timestamp ge datetime'%s' and Timestamp lt datetime'%s'",
                    inicio.format(DateTimeFormatter.ISO_DATE_TIME),
                    fim.format(DateTimeFormatter.ISO_DATE_TIME));

            ListEntitiesOptions options = new ListEntitiesOptions().setFilter(filter);

            List<Avaliacao> avaliacoes = tableClient.listEntities(options, null, null)
                    .stream()
                    .map(AvaliacaoMapper::fromTableEntity)
                    .toList();

            LOG.infof("Total de avaliações encontradas: %d", avaliacoes.size());
            return avaliacoes;
        } catch (Exception e) {
            LOG.errorf("Erro ao buscar avaliações: %s", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void criarTabelaAvaliacoes() {
        try {
            tableServiceClient.createTableIfNotExists(TABLE_AVALIACOES);
        } catch (Exception e) {
            LOG.warnf("Tabela %s pode já existir ou erro ao criar: %s", TABLE_AVALIACOES, e.getMessage());
        }
    }
}
