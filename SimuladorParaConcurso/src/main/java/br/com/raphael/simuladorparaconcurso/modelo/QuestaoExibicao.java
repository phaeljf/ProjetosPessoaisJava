package br.com.raphael.simuladorparaconcurso.modelo;

import br.com.raphael.simuladorparaconcurso.dominio.Questao;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuestaoExibicao {
    public Long id;
    public String areaNome;
    public String enunciado;
    public String a, b, c, d;
    public String correta;
    public String escolhida; // "A".."D" ou null

    public static QuestaoExibicao de(Questao q) {
        QuestaoExibicao v = new QuestaoExibicao();

        v.id = q.getId();
        v.areaNome = q.getAreaConhecimento().getNome();
        v.enunciado = q.getEnunciado();

        List<String> textos = Arrays.asList(
                q.getAlternativaA(),
                q.getAlternativaB(),
                q.getAlternativaC(),
                q.getAlternativaD()
        );

        int idxCorretaOriginal = switch (Character.toUpperCase(q.getCorreta())) {
            case 'A' -> 0;
            case 'B' -> 1;
            case 'C' -> 2;
            default  -> 3; // 'D'
        };

        List<Integer> ordem = Arrays.asList(0, 1, 2, 3);
        Collections.shuffle(ordem);

        v.a = textos.get(ordem.get(0));
        v.b = textos.get(ordem.get(1));
        v.c = textos.get(ordem.get(2));
        v.d = textos.get(ordem.get(3));

        int novaPosicaoCorreta = ordem.indexOf(idxCorretaOriginal);
        v.correta = String.valueOf("ABCD".charAt(novaPosicaoCorreta)); // <-- agora String
        v.escolhida = null; // só pra deixar explícito

        return v;
    }

}
