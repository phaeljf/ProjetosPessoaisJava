package br.com.caixaunica;

import com.jcraft.jsch.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SftpDownloader implements FileDownloader {

    private final ServidorInfo info;      // Dados do servidor, vindos do CSV
    private final LoggerSimples logger;   // Sistema de log
    private Session session;
    private ChannelSftp sftp;

    public SftpDownloader(ServidorInfo info, LoggerSimples logger) {
        this.info = info;
        this.logger = logger;
    }

    // Conecta ao servidor SFTP sem checar fingerprint
    @Override
    public boolean conectar() {
        try {
            JSch jsch = new JSch();

            session = jsch.getSession(info.usuario, info.host, info.porta);
            session.setPassword(info.senha);

            // Desativa verificação de fingerprint (conforme orientação de Let-sama)
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            // Entra na pasta de origem configurada
            sftp.cd(info.pastaOrigem);

            logger.logOk("Conexão SFTP estabelecida com sucesso.");
            return true;

        } catch (JSchException | SftpException e) {
            logger.logErro("Erro ao conectar via SFTP: " + e.getMessage());
            return false;
        }
    }

    // Conta quantos arquivos existem na pasta remota
    @Override
    public int contarArquivos() {
        try {
            Vector<ChannelSftp.LsEntry> arquivos = sftp.ls(".");
            if (arquivos == null) {
                logger.logInfo("Nenhum arquivo encontrado.");
                return 0;
            }
            int total = (int) arquivos.stream().filter(a -> !a.getAttrs().isDir()).count();
            logger.logInfo("Arquivos encontrados no servidor SFTP: " + total);
            return total;
        } catch (SftpException e) {
            logger.logErro("Erro ao contar arquivos: " + e.getMessage());
            return 0;
        }
    }

    // Baixa os arquivos da pasta remota para a pasta local
    @Override
    public int baixarArquivos() {
        int baixados = 0;

        try {
            File pasta = new File(info.getPastaDestinoCompleta());
            if (!pasta.exists()) pasta.mkdirs();

            Vector<ChannelSftp.LsEntry> arquivos = sftp.ls(".");

            for (ChannelSftp.LsEntry entry : arquivos) {
                if (!entry.getAttrs().isDir()) {
                    String nome = entry.getFilename();
                    File destino = new File(pasta, nome);

                    try (FileOutputStream out = new FileOutputStream(destino)) {
                        sftp.get(nome, out);
                        logger.logOk("Arquivo baixado: " + nome + " (" + entry.getAttrs().getSize() + " bytes)");
                        baixados++;
                    } catch (Exception e) {
                        logger.logErro("Erro ao baixar arquivo: " + nome + " - " + e.getMessage());
                    }
                }
            }

        } catch (SftpException e) {
            logger.logErro("Erro durante o download SFTP: " + e.getMessage());
        }

        return baixados;
    }

    // Move arquivos para a pasta remota informada (ex: "Enviados"), sem renomear
    @Override
    public boolean moverArquivos() {
        // Se não há pasta de destino definida, pula movimentação
        if (info.pastaMover == null || info.pastaMover.trim().isEmpty()) {
            logger.logInfo("Movimentação de arquivos desativada para este servidor.");
            return true;
        }

        try {
            // Lista os arquivos da pasta de origem
            Vector<ChannelSftp.LsEntry> arquivos = sftp.ls(info.pastaOrigem);

            for (ChannelSftp.LsEntry entry : arquivos) {
                if (!entry.getAttrs().isDir()) {
                    String nome = entry.getFilename();

                    String origem = construirCaminho(info.pastaOrigem, nome);
                    String destinoDir = construirCaminho(info.pastaOrigem, info.pastaMover);
                    String destino = construirCaminho(destinoDir, nome);

                    try {
                        sftp.rename(origem, destino);
                        logger.logOk("Arquivo movido para /" + info.pastaMover + ": " + nome);
                    } catch (Exception e) {
                        logger.logErro("Erro ao mover arquivo: " + nome + " - " + e.getMessage());
                    }
                }
            }

            return true;

        } catch (SftpException e) {
            logger.logErro("Erro ao listar arquivos para movimentar: " + e.getMessage());
            return false;
        }
    }

    //Constrói um caminho remoto, garantindo que não haja barras duplas ou ausentes.
    //Exemplo: construirCaminho("/pasta", "arquivo.txt")  "/pasta/arquivo.txt"
    private String construirCaminho(String base, String complemento) {
        return base.replaceAll("/+$", "") + "/" + complemento.replaceAll("^/+", "");
    }

    // Encerra a conexão com o servidor
    @Override
    public void fecharConexao() {
        if (sftp != null && sftp.isConnected()) {
            sftp.disconnect();
        }

        if (session != null && session.isConnected()) {
            session.disconnect();
        }

        logger.logInfo("Conexão SFTP encerrada.");
    }

    // Lista os arquivos do servidor (nome → tamanho)
    @Override
    public Map<String, Long> listarArquivosServidor() {
        Map<String, Long> arquivos = new HashMap<>();

        try {
            Vector<ChannelSftp.LsEntry> lista = sftp.ls(".");

            for (ChannelSftp.LsEntry entry : lista) {
                if (!entry.getAttrs().isDir()) {
                    arquivos.put(entry.getFilename(), entry.getAttrs().getSize());
                }
            }

        } catch (SftpException e) {
            logger.logErro("Erro ao listar arquivos no servidor SFTP.");
        }

        return arquivos;
    }

    // Lista os arquivos locais (nome → tamanho)
    @Override
    public Map<String, Long> listarArquivosLocais(String pastaDestino) {
        Map<String, Long> arquivos = new HashMap<>();
        File pasta = new File(pastaDestino);

        if (pasta.exists() && pasta.isDirectory()) {
            for (File file : pasta.listFiles()) {
                if (file.isFile()) {
                    arquivos.put(file.getName(), file.length());
                }
            }
        }

        return arquivos;
    }
}
