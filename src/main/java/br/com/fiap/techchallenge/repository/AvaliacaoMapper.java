package br.com.fiap.techchallenge.repository;

import br.com.fiap.techchallenge.model.Avaliacao;
import com.azure.data.tables.models.TableEntity;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

public class AvaliacaoMapper {

    private static final Logger LOG = Logger.getLogger(AvaliacaoMapper.class);

    public static TableEntity toTableEntity(Avaliacao avaliacao, String partitionKey) {
        return new TableEntity(partitionKey, avaliacao.getId())
                .addProperty("descricao", avaliacao.getDescricao())
                .addProperty("nota", avaliacao.getNota())
                .addProperty("urgencia", avaliacao.getUrgencia() != null ? avaliacao.getUrgencia().toString() : "NORMAL")
                .addProperty("dataHora", avaliacao.getDataHora() != null ? avaliacao.getDataHora().toString() : LocalDateTime.now().toString());
    }

    public static Avaliacao fromTableEntity(TableEntity entity) {
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
