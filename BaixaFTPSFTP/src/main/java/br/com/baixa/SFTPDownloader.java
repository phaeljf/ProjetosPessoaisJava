package br.com.baixa;

import com.jcraft.jsch.*;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

//Classe responsável por realizar o download de arquivos via protocolo SFTP.
public class SFTPDownloader {

    public static void baixarArquivos(ServidorInfo servidor, LoggerSimples logger) {
        JSch jsch = new JSch();

        try {
            // Cria e configura a sessão SSH
            Session session = jsch.getSession(servidor.usuario, servidor.host, 22);
            session.setPassword(servidor.senha);
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect(); // Conecta à sessão

            Channel channel = session.openChannel("sftp");
            channel.connect();

            ChannelSftp sftp = (ChannelSftp) channel;

            logger.registrar("🔗 Conectado via SFTP: " + servidor.host);

            // Entra na pasta de origem definida no CSV
            sftp.cd(servidor.pastaOrigem);

            // Cria a pasta local do cliente
            String destinoLocal = "C:\\Clientes\\" + servidor.pastaDestino;
            Files.createDirectories(Paths.get(destinoLocal));

            // Lista os arquivos no diretório remoto
            Vector<ChannelSftp.LsEntry> arquivos = sftp.ls(".");

            // Baixa cada arquivo
            for (ChannelSftp.LsEntry entry : arquivos) {
                if (!entry.getAttrs().isDir()) {
                    String nome = entry.getFilename();
                    logger.registrar("⬇️ Baixando arquivo: " + nome);

                    try (InputStream input = sftp.get(nome);
                         FileOutputStream output = new FileOutputStream(destinoLocal + "\\" + nome)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }

                    } catch (Exception e) {
                        logger.registrar("⚠️ Erro ao baixar: " + nome + " - " + e.getMessage());
                    }
                }
            }

            // Verifica se a pasta "Enviados" existe
            boolean enviadosExiste = false;
            try {
                sftp.cd("Enviados");
                enviadosExiste = true;
                sftp.cd(".."); // Volta para pasta anterior
            } catch (SftpException e) {
                logger.registrar("📁 Pasta 'Enviados' não encontrada.");
            }

            // Se a pasta "Enviados" existir, move os arquivos para lá
            if (enviadosExiste) {
                for (ChannelSftp.LsEntry entry : arquivos) {
                    if (!entry.getAttrs().isDir()) {
                        String nome = entry.getFilename();
                        try {
                            sftp.rename(nome, "Enviados/" + nome);
                            logger.registrar("📦 Arquivo movido para Enviados: " + nome);
                        } catch (SftpException e) {
                            logger.registrar("⚠️ Falha ao mover: " + nome + " - " + e.getMessage());
                        }
                    }
                }
            }

            // Finaliza a sessão com segurança
            sftp.disconnect();
            session.disconnect();

            logger.registrar("✅ Processo finalizado para: " + servidor.pastaDestino);

        } catch (JSchException | SftpException | java.io.IOException e) {
            logger.registrar("❌ Erro durante operação SFTP: " + e.getMessage());
        }
    }
}
