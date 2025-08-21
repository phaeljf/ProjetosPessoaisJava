package br.com.caixaunica;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FtpDownloader implements FileDownloader {

    private final ServidorInfo info;          // Dados do servidor, vindos do CSV
    private final LoggerSimples logger;       // Sistema de log personalizado
    private FTPClient ftpClient;              // Cliente FTP (Apache Commons Net)

    // Construtor: injeta as dependências da execução
    public FtpDownloader(ServidorInfo info, LoggerSimples logger) {
        this.info = info;
        this.logger = logger;
        this.ftpClient = new FTPClient();
    }

    // Conecta ao servidor FTP, faz login e navega até a pasta de origem
    @Override
    public boolean conectar() {
        try {
            ftpClient.connect(info.host, info.porta);
            ftpClient.login(info.usuario, info.senha);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(info.pastaOrigem);
            logger.logOk("Conectado ao servidor FTP com sucesso.");
            return true;
        } catch (IOException e) {
            logger.logErro("Erro ao conectar via FTP: " + e.getMessage());
            return false;
        }
    }

    // Conta quantos arquivos estão disponíveis na pasta remota
    @Override
    public int contarArquivos() {
        try {
            FTPFile[] arquivos = ftpClient.listFiles();
            logger.logInfo("Arquivos encontrados no servidor: " + arquivos.length);
            return arquivos.length;
        } catch (IOException e) {
            logger.logErro("Erro ao contar arquivos: " + e.getMessage());
            return 0;
        }
    }

    // Baixa todos os arquivos da pasta remota para a pasta local
    @Override
    public int baixarArquivos() {
        int baixados = 0;

        try {
            File pastaLocal = new File(info.getPastaDestinoCompleta());
            if (!pastaLocal.exists()) pastaLocal.mkdirs();

            FTPFile[] arquivos = ftpClient.listFiles();

            for (FTPFile file : arquivos) {
                if (file.isFile()) {
                    File destino = new File(pastaLocal, file.getName());

                    try (FileOutputStream out = new FileOutputStream(destino)) {
                        boolean sucesso = ftpClient.retrieveFile(file.getName(), out);

                        if (sucesso) {
                            logger.logOk("Arquivo baixado: " + file.getName() + " (" + file.getSize() + " bytes)");
                            baixados++;
                        } else {
                            logger.logErro("Falha ao baixar: " + file.getName());
                        }
                    }
                }
            }

        } catch (IOException e) {
            logger.logErro("Erro durante o download FTP: " + e.getMessage());
        }

        return baixados;
    }

    // Move os arquivos para a subpasta informada no campo "pastaMover", se configurada
    @Override
    public boolean moverArquivos() {
        // Se pastaMover estiver vazia, não mover
        if (info.pastaMover == null || info.pastaMover.trim().isEmpty()) {
            logger.logInfo("Movimentação de arquivos desativada para este servidor.");
            return true;
        }

        try {
            FTPFile[] arquivos = ftpClient.listFiles();

            for (FTPFile file : arquivos) {
                if (file.isFile()) {
                    // Caminho original e destino com o mesmo nome, apenas em outra pasta
                    String origem = info.pastaOrigem + "/" + file.getName();
                    String destino = info.pastaOrigem + "/" + info.pastaMover + "/" + file.getName();

                    // Move o arquivo (sem renomear nem criar pasta)
                    boolean moved = ftpClient.rename(origem, destino);

                    if (moved) {
                        logger.logOk("Arquivo movido para /" + info.pastaMover + ": " + file.getName());
                    } else {
                        logger.logErro("Erro ao mover arquivo para /" + info.pastaMover + ": " + file.getName());
                    }
                }
            }

            return true;

        } catch (IOException e) {
            logger.logErro("Erro ao mover arquivos: " + e.getMessage());
            return false;
        }
    }

    // Fecha a conexão FTP com elegância
    @Override
    public void fecharConexao() {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                logger.logInfo("Conexão FTP encerrada.");
            }
        } catch (IOException e) {
            logger.logErro("Erro ao fechar conexão FTP: " + e.getMessage());
        }
    }

    // Lista os arquivos na pasta remota (nome → tamanho)
    @Override
    public Map<String, Long> listarArquivosServidor() {
        Map<String, Long> arquivos = new HashMap<>();

        try {
            FTPFile[] lista = ftpClient.listFiles();

            for (FTPFile file : lista) {
                if (file.isFile()) {
                    arquivos.put(file.getName(), file.getSize());
                }
            }

        } catch (IOException e) {
            logger.logErro("Erro ao listar arquivos no servidor FTP.");
        }

        return arquivos;
    }

    // Lista os arquivos já baixados na pasta local (nome → tamanho)
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
