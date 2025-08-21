package br.com.prosistema;

import br.com.prosistema.core.ConversorJson;
import br.com.prosistema.util.Logger;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            Logger.erro("Uso correto: java -jar ConversorJson.jar <pastaOrigem> <pastaDestino>");
            return;
        }

        String pastaOrigem = args[0];
        String pastaDestino = args[1];

        File origem = new File(pastaOrigem);
        File destino = new File(pastaDestino);

        if (!origem.exists() || !origem.isDirectory()) {
            Logger.erro("Pasta de origem inválida: " + pastaOrigem);
            return;
        }

        if (!destino.exists()) {
            boolean criada = destino.mkdirs();
            if (criada) {
                Logger.info("Pasta de destino criada: " + destino.getAbsolutePath());
            } else {
                Logger.erro("Não foi possível criar a pasta de destino.");
                return;
            }
        }

        ConversorJson conversor = new ConversorJson();
        conversor.executar(origem, destino);
    }
}
