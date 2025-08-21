package br.com.caixaunica;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FtpsDownloader implements FileDownloader {

    private final ServidorInfo info;
    private final LoggerSimples logger;
    private FTPSClient ftpClient;

    public FtpsDownloader(ServidorInfo info, LoggerSimples logger) {
        this.info = info;
        this.logger = logger;
        this.ftpClient = new FTPSClient("TLS", false);
    }

    @Override
    public boolean conectar() {
        try {
            ftpClient.connect(info.host, info.porta);
            ftpClient.login(info.usuario, info.senha);
            ftpClient.execPBSZ(0);
            ftpClient.execPROT("P");
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(info.pastaOrigem);
            logger.logOk("Conectado ao servidor FTPS com sucesso.");
            return true;
        } catch (IOException e) {
            logger.logErro("Erro ao conectar via FTPS: " + e.getMessage());
            return false;
        }
    }

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
            logger.logErro("Erro durante o download FTPS: " + e.getMessage());
        }

        return baixados;
    }

    @Override
    public boolean moverArquivos() {
        if (info.pastaMover == null || info.pastaMover.trim().isEmpty()) {
            logger.logInfo("Movimentação de arquivos desativada para este servidor.");
            return true;
        }

        try {
            FTPFile[] arquivos = ftpClient.listFiles();

            for (FTPFile file : arquivos) {
                if (file.isFile()) {
                    String origem = info.pastaOrigem + "/" + file.getName();
                    String destino = info.pastaOrigem + "/" + info.pastaMover + "/" + file.getName();

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

    @Override
    public void fecharConexao() {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                logger.logInfo("Conexão FTPS encerrada.");
            }
        } catch (IOException e) {
            logger.logErro("Erro ao fechar conexão FTPS: " + e.getMessage());
        }
    }

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
            logger.logErro("Erro ao listar arquivos no servidor FTPS.");
        }

        return arquivos;
    }

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
