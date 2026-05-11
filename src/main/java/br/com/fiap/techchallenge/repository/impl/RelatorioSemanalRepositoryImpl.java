package br.com.fiap.techchallenge.repository.impl;

import br.com.fiap.techchallenge.exception.SalvarRelatorioException;
import br.com.fiap.techchallenge.model.RelatorioSemanal;
import br.com.fiap.techchallenge.repository.RelatorioSemanalMapper;
import br.com.fiap.techchallenge.repository.RelatorioSemanalRepository;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.models.TableEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RelatorioSemanalRepositoryImpl implements RelatorioSemanalRepository {

    private static final Logger LOG = Logger.getLogger(RelatorioSemanalRepositoryImpl.class);
    private static final String TABLE_RELATORIOS = "relatorios";
    private static final String PARTITION_KEY_RELATORIOS = "Semanal";

    private final TableServiceClient tableServiceClient;

    @Inject
    public RelatorioSemanalRepositoryImpl(TableServiceClient tableServiceClient) {
        this.tableServiceClient = tableServiceClient;
        criarTabelaRelatorios();
    }

    @Override
    public void salvar(RelatorioSemanal relatorio) {
        try {
            LOG.infof("Salvando relatório: %s", relatorio.getId());

            TableEntity entity = RelatorioSemanalMapper.toTableEntity(relatorio, PARTITION_KEY_RELATORIOS);

            tableServiceClient.getTableClient(TABLE_RELATORIOS).createEntity(entity);
            LOG.infof("Relatório salvo com sucesso: %s", relatorio.getId());

        } catch (Exception e) {
            LOG.errorf("Erro ao salvar relatório: %s", e.getMessage());
            throw new SalvarRelatorioException("Erro ao salvar relatório", e);
        }
    }

    private void criarTabelaRelatorios() {
        try {
            tableServiceClient.createTableIfNotExists(TABLE_RELATORIOS);
        } catch (Exception e) {
            LOG.warnf("Tabela %s pode já existir ou erro ao criar: %s", TABLE_RELATORIOS, e.getMessage());
        }
    }
}
