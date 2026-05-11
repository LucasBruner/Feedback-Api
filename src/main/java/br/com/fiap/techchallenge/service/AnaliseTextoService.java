package br.com.fiap.techchallenge.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AnaliseTextoService {

    private static final Logger LOG = Logger.getLogger(AnaliseTextoService.class);
    
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "o", "e", "de", "do", "da", "em", "um", "uma", "para", "com", "não",
            "é", "que", "se", "na", "por", "mais", "as", "os", "como", "mas", "foi",
            "ao", "ele", "das", "tem", "à", "seu", "sua", "ou", "ser", "quando",
            "muito", "há", "nos", "já", "está", "eu", "também", "só", "pelo", "pela",
            "até", "isso", "ela", "entre", "era", "depois", "sem", "mesmo", "aos",
            "ter", "seus", "suas", "numa", "pelos", "pelas", "num", "nem",
            "meu", "às", "minha", "têm"
    );

    private static final int MIN_PALAVRA_LENGTH = 3;
    
    private static final int MAX_RESULTADOS = 10;

    public Map<String, Long> analisarPalavrasRecorrentes(List<String> descricoes) {
        if (descricoes == null || descricoes.isEmpty()) {
            LOG.warn("Lista de descrições vazia para análise");
            return new LinkedHashMap<>();
        }

        LOG.infof("Analisando %d descrições para identificar palavras recorrentes", descricoes.size());

        Map<String, Long> frequenciaPalavras = descricoes.stream()
                .filter(Objects::nonNull)
                .filter(desc -> !desc.trim().isEmpty())
                .flatMap(desc -> extrairPalavras(desc).stream())
                .filter(palavra -> palavra.length() >= MIN_PALAVRA_LENGTH)
                .map(String::toLowerCase)
                .filter(lowerCase -> !STOP_WORDS.contains(lowerCase))
                .collect(Collectors.groupingBy(
                        palavra -> palavra,
                        Collectors.counting()
                ));

        Map<String, Long> palavrasRecorrentes = frequenciaPalavras.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(MAX_RESULTADOS)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        LOG.infof("Identificadas %d palavras recorrentes", palavrasRecorrentes.size());
        return palavrasRecorrentes;
    }

    public Map<String, Long> analisarFrasesRecorrentes(List<String> descricoes) {
        if (descricoes == null || descricoes.isEmpty()) {
            LOG.warn("Lista de descrições vazia para análise de frases");
            return new LinkedHashMap<>();
        }

        LOG.infof("Analisando %d descrições para identificar frases recorrentes", descricoes.size());

        Map<String, Long> frequenciaFrases = descricoes.stream()
                .filter(Objects::nonNull)
                .filter(desc -> !desc.trim().isEmpty())
                .flatMap(desc -> extrairFrases(desc).stream())
                .filter(frase -> !frase.trim().isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.groupingBy(
                        frase -> frase,
                        Collectors.counting()
                ));

        Map<String, Long> frasesRecorrentes = frequenciaFrases.entrySet().stream()
                .filter(entry -> entry.getValue() >= 2)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(MAX_RESULTADOS)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        LOG.infof("Identificadas %d frases recorrentes", frasesRecorrentes.size());
        return frasesRecorrentes;
    }

    private List<String> extrairPalavras(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String textoLimpo = texto.replaceAll("[^\\p{L}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return Arrays.stream(textoLimpo.split("\\s+"))
                .filter(palavra -> !palavra.isEmpty())
                .toList();
    }

    private List<String> extrairFrases(String texto) {
        List<String> palavras = extrairPalavras(texto);
        List<String> palavrasSemStopWords = palavras.stream()
                .map(String::toLowerCase)
                .filter(p -> !STOP_WORDS.contains(p))
                .toList();

        List<String> frases = new ArrayList<>();

        if (palavrasSemStopWords.size() < 2) {
            return frases;
        }

        for (int i = 0; i < palavrasSemStopWords.size() - 1; i++) {
            frases.add(palavrasSemStopWords.get(i) + " " + palavrasSemStopWords.get(i + 1));
        }

        if (palavrasSemStopWords.size() >= 3) {
            for (int i = 0; i < palavrasSemStopWords.size() - 2; i++) {
                frases.add(palavrasSemStopWords.get(i) + " " + palavrasSemStopWords.get(i + 1) + " " + palavrasSemStopWords.get(i + 2));
            }
        }

        return frases;
    }
}
