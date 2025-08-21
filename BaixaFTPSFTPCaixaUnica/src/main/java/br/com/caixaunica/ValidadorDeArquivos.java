package br.com.caixaunica;

import java.util.Map;

public class ValidadorDeArquivos {

    // Metodo principal de comparação
    public static RelatorioComparacao comparar(Map<String, Long> remotos, Map<String, Long> locais, LoggerSimples logger) {
        int ok = 0;
        int erroTamanho = 0;
        int ausentes = 0;

        for (Map.Entry<String, Long> remoto : remotos.entrySet()) {
            String nome = remoto.getKey();
            Long tamanhoEsperado = remoto.getValue();

            if (!locais.containsKey(nome)) {
                ausentes++;
                logger.logErro("Arquivo NÃO BAIXADO: " + nome + " (" + tamanhoEsperado + " bytes)");
            } else {
                Long tamanhoLocal = locais.get(nome);
                if (!tamanhoEsperado.equals(tamanhoLocal)) {
                    erroTamanho++;
                    logger.logErro("TAMANHO INCORRETO: " + nome +
                            " → Esperado: " + tamanhoEsperado + ", Local: " + tamanhoLocal);
                } else {
                    ok++;
                    logger.logOk("Arquivo verificado com sucesso: " + nome + " (" + tamanhoEsperado + " bytes)");
                }
            }
        }

        return new RelatorioComparacao(ok, erroTamanho, ausentes);
    }

    // Classe interna representando o resultado da comparação
    public static class RelatorioComparacao {
        private final int ok;
        private final int erroTamanho;
        private final int ausentes;

        public RelatorioComparacao(int ok, int erroTamanho, int ausentes) {
            this.ok = ok;
            this.erroTamanho = erroTamanho;
            this.ausentes = ausentes;
        }

        public int ok() {
            return ok;
        }

        public int erroTamanho() {
            return erroTamanho;
        }

        public int ausentes() {
            return ausentes;
        }
    }
}
