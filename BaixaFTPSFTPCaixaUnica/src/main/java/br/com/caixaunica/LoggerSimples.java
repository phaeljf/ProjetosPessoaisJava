package br.com.caixaunica;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerSimples {

    private final PrintWriter writer;

    public LoggerSimples(String administradora) {
        try {
            // Monta caminho do arquivo de log por administradora e data
            String data = LocalDate.now().toString(); // AAAA-MM-DD
            String caminhoPasta = "C:\\Logs\\BaixaFTPUnica\\" + administradora;
            String caminhoArquivo = caminhoPasta + "\\" + administradora + "-" + data + ".log";

            // Garante que a pasta exista
            new File(caminhoPasta).mkdirs();

            // Abre writer com buffer e append
            writer = new PrintWriter(new BufferedWriter(new FileWriter(caminhoArquivo, true)), true);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao inicializar LoggerSimples: " + e.getMessage());
        }
    }

    public void logInfo(String mensagem) {
        escrever("[INFO] " + mensagem);
    }

    public void logOk(String mensagem) {
        escrever("[OK]   " + mensagem);
    }

    public void logErro(String mensagem) {
        escrever("[ERRO] " + mensagem);
    }

    private void escrever(String msg) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        writer.println(timestamp + " " + msg);
    }

    // Novo: fecha explicitamente o writer
    public void fechar() {
        writer.close();
    }
}
