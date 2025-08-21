package br.com.baixa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerSimples {

    // Caminho base onde os logs serão salvos
    private static final String PASTA_LOG = "C:\\Logs\\BaixaFTP";

    // Formato de data/hora para cada linha do log
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Caminho completo do arquivo de log deste cliente
    private final String caminhoLog;

    // Construtor que recebe o nome da "pasta destino" e define o arquivo de log
    public LoggerSimples(String pastaDestino) {
        // Cria a pasta de logs, se não existir
        new File(PASTA_LOG).mkdirs();

        // Sanitize no nome do arquivo para evitar problemas com barras
        String nomeArquivoLog = pastaDestino.replaceAll("[\\\\/]", "_");

        // Define o caminho completo do log
        this.caminhoLog = PASTA_LOG + "\\" + nomeArquivoLog + ".log";
    }

    // Escreve uma linha no log, com timestamp
    public synchronized void registrar(String mensagem) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoLog, true))) {
            String timestamp = LocalDateTime.now().format(FORMATADOR_TIMESTAMP);
            writer.write("[" + timestamp + "] " + mensagem);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("⚠️ Falha ao escrever no log (" + caminhoLog + "): " + e.getMessage());
        }
    }
}
