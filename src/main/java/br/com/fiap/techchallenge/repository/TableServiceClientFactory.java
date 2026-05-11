package br.com.fiap.techchallenge.repository;

import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TableServiceClientFactory {

    private static final Logger LOG = Logger.getLogger(TableServiceClientFactory.class);

    @Produces
    @ApplicationScoped
    public TableServiceClient createTableServiceClient() {
        LOG.info("Inicializando conexão com Azure Storage Tables");
        String connectionString = System.getenv("AzureWebJobsStorage");
        if (connectionString == null || connectionString.isBlank()) {
            connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        }
        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException("Connection string do Azure Storage não configurada. Defina AzureWebJobsStorage ou AZURE_STORAGE_CONNECTION_STRING.");
        }
        LOG.info("Conexão estabelecida com sucesso.");
        return new TableServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }
}
