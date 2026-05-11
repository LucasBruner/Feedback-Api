package br.com.fiap.techchallenge.repository;

import br.com.fiap.techchallenge.model.RelatorioSemanal;
import com.azure.data.tables.models.TableEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;

public class RelatorioSemanalMapper {

    private static final String FORMAT_STRING = "\"%s\":%d";

    public static TableEntity toTableEntity(RelatorioSemanal relatorio, String partitionKey) {
        TableEntity entity = criarEntityBase(relatorio, partitionKey);
        adicionarAvaliacoesPorDia(entity, relatorio.getAvaliacoesPorDia());
        adicionarContagemPorUrgencia(entity, relatorio.getContagemPorUrgencia());
        adicionarMapaComoJson(entity, "palavrasRecorrentes", relatorio.getPalavrasRecorrentes());
        adicionarMapaComoJson(entity, "frasesRecorrentes", relatorio.getFrasesRecorrentes());
        return entity;
    }

    private static TableEntity criarEntityBase(RelatorioSemanal relatorio, String partitionKey) {
        String dataGeracao = relatorio.getDataGeracao() != null
                ? relatorio.getDataGeracao().toString()
                : LocalDateTime.now().toString();

        String periodoInicio = relatorio.getPeriodoInicio() != null
                ? relatorio.getPeriodoInicio().toString()
                : "";

        String periodoFim = relatorio.getPeriodoFim() != null
                ? relatorio.getPeriodoFim().toString()
                : "";

        return new TableEntity(partitionKey, relatorio.getId())
                .addProperty("dataGeracao", dataGeracao)
                .addProperty("periodoInicio", periodoInicio)
                .addProperty("periodoFim", periodoFim)
                .addProperty("totalAvaliacoes", relatorio.getTotalAvaliacoes())
                .addProperty("mediaNotas", relatorio.getMediaNotas())
                .addProperty("notaMaisAlta", Objects.requireNonNullElse(relatorio.getNotaMaisAlta(), 0))
                .addProperty("notaMaisBaixa", Objects.requireNonNullElse(relatorio.getNotaMaisBaixa(), 0));
    }

    private static void adicionarAvaliacoesPorDia(TableEntity entity,
                                                  Map<String, Long> avaliacoesPorDia) {
        if (avaliacoesPorDia == null) return;

        String json = serializarMapaParaJson(
                avaliacoesPorDia,
                dia -> dia,
                valor -> valor
        );

        entity.addProperty("avaliacoesPorDia", json);
    }

    private static void adicionarContagemPorUrgencia(TableEntity entity,
                                                     Map<String, Long> contagemPorUrgencia) {
        if (contagemPorUrgencia == null) return;

        contagemPorUrgencia.forEach((nivel, count) ->
                entity.addProperty("contagem_" + nivel, count));
    }

    private static void adicionarMapaComoJson(TableEntity entity, String propriedade, Map<String, Long> mapa) {
        if (mapa == null || mapa.isEmpty()) return;

        String json = serializarMapaParaJson(
                mapa,
                chave -> sanitizarParaJson(Objects.requireNonNullElse(chave, "")),
                valor -> valor
        );

        entity.addProperty(propriedade, json);
    }

    private static <K> String serializarMapaParaJson(
            Map<K, Long> mapa,
            Function<K, String> serializarChave,
            LongUnaryOperator serializarValor) {

        String conteudo = mapa.entrySet().stream()
                .map(entry -> String.format(FORMAT_STRING,
                        serializarChave.apply(entry.getKey()),
                        serializarValor.applyAsLong(Objects.requireNonNullElse(entry.getValue(), 0L))))
                .collect(Collectors.joining(","));

        return "{" + conteudo + "}";
    }

    private static String sanitizarParaJson(String valor) {
        return valor.replace("\"", "\\\"");
    }
}
