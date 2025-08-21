package br.com.prosistema.service;

import br.com.prosistema.model.recebiveis.ArquivoRecebiveis;
import br.com.prosistema.service.ConversaoRecebiveis;
import br.com.prosistema.util.LoggerUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class ConversorJson {

    private final ObjectMapper mapper = new ObjectMapper();

    public void executarConversao(File pastaEntrada, File pastaSaida) {
        LoggerUtil.info("ConversorJson", "Lendo arquivos da pasta: " + pastaEntrada.getAbsolutePath());

        File[] arquivos = pastaEntrada.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (arquivos == null || arquivos.length == 0) {
            LoggerUtil.aviso("ConversorJson", "Nenhum arquivo JSON encontrado.");
            return;
        }

        for (File arquivo : arquivos) {
            try {
                LoggerUtil.info("ConversorJson", "Lendo arquivo: " + arquivo.getName());
                JsonNode raiz = mapper.readTree(arquivo);
                String tipo = raiz.path("fileHeader").path("fileTypeDescription").asText();

                switch (tipo) {
                    case "Movimento de Recebíveis" -> {

                    }

                    default -> LoggerUtil.aviso("ConversorJson", "Tipo de arquivo não reconhecido: " + tipo + " (" + arquivo.getName() + ")");
                }

            } catch (Exception e) {
                LoggerUtil.erro("ConversorJson", "Erro ao processar " + arquivo.getName() + ": " + e.getMessage());
            }
        }

        LoggerUtil.sucesso("ConversorJson", "Todos os arquivos processados com sucesso.");
    }
}
