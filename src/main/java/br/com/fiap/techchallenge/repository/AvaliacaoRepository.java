package br.com.fiap.techchallenge.repository;

import br.com.fiap.techchallenge.model.Avaliacao;

import java.time.LocalDateTime;
import java.util.List;

public interface AvaliacaoRepository {
    void salvar(Avaliacao avaliacao);
    List<Avaliacao> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim);
}
