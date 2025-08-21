package br.com.baixa;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Marca o início da execução para calcular o tempo total
        long inicio = System.currentTimeMillis();

        System.out.println("📄 Iniciando leitura da planilha...");

        // Cria as pastas principais se não existirem
        // Evita erros ao salvar arquivos de clientes ou logs
        new File("C:\\Clientes").mkdirs();
        new File("C:\\Logs\\BaixaFTP").mkdirs();

        // Verifica se o caminho do CSV foi passado como argumento
        if (args.length == 0) {
            System.out.println("❌ Por favor, informe o caminho do CSV como argumento.");
            System.out.println("Exemplo: java -jar baixa-ftpsftp-1.0.0.jar \"D:\\FTP\\lista.csv\"");
            return;
        }

        // Caminho do arquivo CSV passado por linha de comando
        String caminhoCSV = args[0];
        System.out.println("📄 Lendo: " + caminhoCSV);

        // Lê a planilha CSV e transforma cada linha em um objeto ServidorInfo
        List<ServidorInfo> servidores = CSVReader.lerCSV(caminhoCSV);
        System.out.println("🔍 " + servidores.size() + " servidores encontrados.");

        // Processa cada linha da planilha
        for (ServidorInfo servidor : servidores) {

            // Cria um logger individual para o cliente (baseado em pastaDestino)
            LoggerSimples logger = new LoggerSimples(servidor.pastaDestino);

            // Início da seção de log
            logger.registrar("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            logger.registrar("🔧 Iniciando processo para: " + servidor);

            // Determina o tipo de conexão (FTP ou SFTP)
            String tipo = servidor.isSFTP() ? "SFTP" : servidor.isFTP() ? "FTP" : "Desconhecido";
            logger.registrar("🔗 Tipo de conexão detectado: " + tipo);

            try {
                // Escolhe a estratégia correta de download
                if (servidor.isFTP()) {
                    FTPDownloader.baixarArquivos(servidor, logger);
                } else if (servidor.isSFTP()) {
                    SFTPDownloader.baixarArquivos(servidor, logger);
                } else {
                    logger.registrar("❌ Tipo de conexão desconhecido para: " + servidor.host);
                }
            } catch (Exception e) {
                // Captura qualquer erro durante o processo
                logger.registrar("❌ Erro ao processar " + servidor.host + ": " + e.getMessage());
            }

            // Final da seção de log por cliente
            logger.registrar("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }

        // Calcula o tempo total de execução
        long fim = System.currentTimeMillis();
        long duracaoSegundos = (fim - inicio) / 1000;

        System.out.println("🏁 Execução finalizada. Tempo total: " + duracaoSegundos + " segundos.");
    }
}
