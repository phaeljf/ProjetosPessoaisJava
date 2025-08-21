package br.com.prosistema.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {

    private static final String CAMINHO_LOG = "C:\\logs\\Conversor"; // ✨ Caminho padrão (pode parametrizar depois)

    public static void log(String categoria, String mensagem) {
        try {
            String dataHoje = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String nomeArquivo = categoria + "-" + dataHoje + ".log";
            File pasta = new File(CAMINHO_LOG + File.separator + categoria);
            if (!pasta.exists()) {
                pasta.mkdirs();
            }

            File arquivo = new File(pasta, nomeArquivo);
            PrintWriter writer = new PrintWriter(new FileWriter(arquivo, true));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.println("[" + timestamp + "] " + mensagem);
            writer.close();
        } catch (Exception e) {
            System.err.println("✖ Erro ao gravar log (" + categoria + "): " + e.getMessage());
        }
    }

    public static void sucesso(String categoria, String mensagem) {
        log(categoria, "✔ SUCESSO: " + mensagem);
    }

    public static void erro(String categoria, String mensagem) {
        log(categoria, "✖ ERRO: " + mensagem);
    }

    public static void aviso(String categoria, String mensagem) {
        log(categoria, "⚠ AVISO: " + mensagem);
    }

    public static void info(String categoria, String mensagem) {
        log(categoria, "ℹ INFO: " + mensagem);
    }
}
