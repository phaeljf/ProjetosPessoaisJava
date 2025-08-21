package br.com.prosistema.core;

import br.com.prosistema.TipoArquivos.ConversaoVendas;
import br.com.prosistema.TipoArquivos.ConversaoPagamentos;
import br.com.prosistema.TipoArquivos.ConversaoRecebiveis;
import br.com.prosistema.util.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class ConversorJson {

    public void executar(File pastaOrigem, File pastaDestino) {
        Logger.info("Iniciando leitura da pasta: " + pastaOrigem.getAbsolutePath());

        File[] arquivos = pastaOrigem.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (arquivos == null || arquivos.length == 0) {
            Logger.info("Nenhum arquivo JSON encontrado.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();

        for (File json : arquivos) {
            Logger.info("Processando: " + json.getName());

            try {
                JsonNode raiz = mapper.readTree(json);
                JsonNode fileHeader = raiz.path("fileHeader");

                if (fileHeader.isMissingNode()) {
                    Logger.erro("Bloco 'fileHeader' ausente no arquivo: " + json.getName());
                    continue;
                }

                String tipoInterno = fileHeader.path("fileTypeDescription").asText("").trim().toLowerCase();

                switch (tipoInterno) {
                    case "movimento financeiro":
                        new ConversaoPagamentos().converter(json, pastaDestino);
                        break;

                    case "movimento de vendas":
                        new ConversaoVendas().converter(json, pastaDestino);
                        break;

                    case "movimento de recebíveis":
                        new ConversaoRecebiveis().converter(json, pastaDestino);
                        break;

                    default:
                        Logger.erro("Tipo de arquivo desconhecido: " + tipoInterno + " em " + json.getName());
                        break;
                }

                String nomeCsv = json.getName().replace(".json", ".csv");
                File csvGerado = new File(pastaDestino, nomeCsv);
                Logger.info("Arquivo convertido com sucesso: " + csvGerado.getAbsolutePath());

            } catch (Exception e) {
                Logger.erro("Erro ao processar " + json.getName() + ": " + e.getMessage());
            }
        }

        Logger.info("Processamento concluído.");
    }
}
