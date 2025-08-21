package br.com.raphael.simuladorparaconcurso.servico;

import br.com.raphael.simuladorparaconcurso.dominio.Questao;
import br.com.raphael.simuladorparaconcurso.modelo.QuestaoExibicao;
import br.com.raphael.simuladorparaconcurso.repositorio.AreaConhecimentoRepositorio;
import br.com.raphael.simuladorparaconcurso.repositorio.QuestaoRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SimuladoServico {

    private final QuestaoRepositorio questaoRepositorio;
    private final AreaConhecimentoRepositorio areaRepositorio;

    public SimuladoServico(QuestaoRepositorio questaoRepositorio,
                           AreaConhecimentoRepositorio areaRepositorio) {
        this.questaoRepositorio = questaoRepositorio;
        this.areaRepositorio = areaRepositorio;
    }

    public List<QuestaoExibicao> montarSimulado(LinkedHashMap<Long, Integer> quantidadesPorArea) throws BadRequestException {
        List<QuestaoExibicao> lista = new ArrayList<>();
        for (Map.Entry<Long,Integer> e : quantidadesPorArea.entrySet()) {
            Long areaId = e.getKey();
            int n = e.getValue();

            int disponiveis = questaoRepositorio.countByAreaConhecimentoIdAndAtivoTrue(areaId);
            if (disponiveis < n) {
                log.error("Area {} com apenas {} questoes ativas.",areaId,disponiveis);
            }
            List<Questao> sorteadas = questaoRepositorio.buscarAleatoriasPorArea(areaId, n);
            sorteadas.stream().map(QuestaoExibicao::de).forEach(lista::add);
        }
        return lista;
    }

    public int corrigir(List<QuestaoExibicao> qs) {
        int acertos = 0;
        for (QuestaoExibicao q : qs) {
            if (q.escolhida != null && q.escolhida.equals(String.valueOf(q.correta))) acertos++;
        }
        return acertos;
    }
}
