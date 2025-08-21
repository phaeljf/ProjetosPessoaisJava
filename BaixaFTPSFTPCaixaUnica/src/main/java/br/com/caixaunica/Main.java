package br.com.caixaunica;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("⚠️ Caminho do CSV não informado.");
            System.out.println("   Exemplo: java -jar app.jar HostsAdmin.csv");
            return;
        }

        String caminhoCSV = args[0];

        // Lê os servidores
        List<ServidorInfo> servidores = CSVReader.lerCSV(caminhoCSV);

        for (ServidorInfo info : servidores) {
            if (!info.ativo) continue;

            System.out.println("▶ Iniciando processamento para: " + info.administradora);
            LoggerSimples logger = new LoggerSimples(info.administradora);
            FileDownloader downloader;

            if (info.isSftp()) {
                downloader = new SftpDownloader(info, logger);
            } else if ("FTPS".equalsIgnoreCase(info.protocolo)) {
                downloader = new FtpsDownloader(info, logger);
            } else {
                downloader = new FtpDownloader(info, logger);
            }

            try {
                logger.logInfo("Conectando a " + info.protocolo.toUpperCase() + "://" + info.host);

                if (!downloader.conectar()) {
                    logger.logErro("Falha ao conectar.");
                    continue;
                }

                downloader.contarArquivos();
                int baixados = downloader.baixarArquivos();
                logger.logInfo("Arquivos baixados: " + baixados);

                Map<String, Long> arquivosRemotos = downloader.listarArquivosServidor();
                Map<String, Long> arquivosLocais = downloader.listarArquivosLocais(info.getPastaDestinoCompleta());

                ValidadorDeArquivos.RelatorioComparacao resumo = ValidadorDeArquivos.comparar(
                        arquivosRemotos, arquivosLocais, logger
                );

                logger.logInfo("Resumo:");
                logger.logInfo("OK: " + resumo.ok());
                logger.logInfo("Ausentes: " + resumo.ausentes());
                logger.logInfo("Tamanho divergente: " + resumo.erroTamanho());

                downloader.moverArquivos();

            } catch (Exception e) {
                logger.logErro("Erro inesperado: " + e.getMessage());
                e.printStackTrace();
            } finally {
                downloader.fecharConexao();
                logger.fechar();
            }

            System.out.println("Finalizado: " + info.administradora);
        }

        System.out.println("Todos os servidores foram processados.");
    }
}
