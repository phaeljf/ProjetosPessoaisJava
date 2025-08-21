package br.com.baixa;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

//Responsável por realizar o download de arquivos via protocolo FTP.

public class FTPDownloader {

    public static void baixarArquivos(ServidorInfo servidor, LoggerSimples logger) {
        FTPClient ftp = new FTPClient();

        try {
            // Conexão com o servidor FTP
            logger.registrar("🔗 Conectando ao FTP: " + servidor.host);
            ftp.connect(servidor.host, 21);
            boolean login = ftp.login(servidor.usuario, servidor.senha);

            if (!login) {
                logger.registrar("❌ Falha no login: " + servidor.usuario);
                return;
            }

            ftp.enterLocalPassiveMode(); // Modo passivo para evitar problemas de firewall
            ftp.setFileType(FTP.BINARY_FILE_TYPE); // Sempre binário para evitar corrompimento

            // Tenta acessar a pasta de origem no servidor
            boolean pastaOk = ftp.changeWorkingDirectory(servidor.pastaOrigem);
            if (!pastaOk) {
                logger.registrar("❌ Pasta de origem não encontrada: " + servidor.pastaOrigem);
                return;
            }

            // Lista os arquivos da pasta atual
            FTPFile[] arquivos = ftp.listFiles();

            // Cria a pasta local onde os arquivos serão salvos
            String destinoLocal = "C:\\Clientes\\" + servidor.pastaDestino;
            Files.createDirectories(Paths.get(destinoLocal));

            // Itera sobre os arquivos e baixa um por um
            for (FTPFile arquivo : arquivos) {
                if (arquivo.isFile()) {
                    String nome = arquivo.getName();
                    logger.registrar("⬇️ Baixando arquivo: " + nome);

                    try (OutputStream output = new FileOutputStream(destinoLocal + "\\" + nome)) {
                        boolean sucesso = ftp.retrieveFile(nome, output);
                        if (!sucesso) {
                            logger.registrar("⚠️ Falha ao baixar: " + nome);
                        }
                    }
                }
            }

            // Verifica se existe a pasta "Enviados" dentro da pasta de origem
            if (ftp.changeWorkingDirectory(servidor.pastaOrigem + "/Enviados")) {
                // Volta para a pasta original antes de mover
                ftp.changeWorkingDirectory(servidor.pastaOrigem);

                for (FTPFile arquivo : arquivos) {
                    if (arquivo.isFile()) {
                        String nome = arquivo.getName();
                        boolean sucesso = ftp.rename(nome, "Enviados/" + nome);
                        if (sucesso) {
                            logger.registrar("📁 Arquivo movido para Enviados: " + nome);
                        } else {
                            logger.registrar("⚠️ Falha ao mover: " + nome);
                        }
                    }
                }
            } else {
                logger.registrar("📁 Pasta 'Enviados' não encontrada.");
            }

            // Finaliza a conexão com segurança
            ftp.logout();
            ftp.disconnect();

            logger.registrar("✅ Processo finalizado para: " + servidor.pastaDestino);

        } catch (IOException e) {
            logger.registrar("❌ Erro durante operação FTP: " + e.getMessage());
        }
    }
}
