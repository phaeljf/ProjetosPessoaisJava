package br.com.raphael.simuladorparaconcurso.modelo;

import java.time.Instant;
import java.util.List;

public class SessaoSimulado {
    public String nomeCandidato;
    public List<EscolhaArea> escolhas;
    public List<QuestaoExibicao> questoes;
    public int indiceAtual;
    public Instant fim; // endTime
}
