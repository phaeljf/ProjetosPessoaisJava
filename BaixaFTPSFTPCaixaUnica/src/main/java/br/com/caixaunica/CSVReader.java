package br.com.caixaunica;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    // Método que lê o CSV e converte em lista de objetos ServidorInfo
    public static List<ServidorInfo> lerCSV(String caminhoArquivo) {
        List<ServidorInfo> servidores = new ArrayList<>();

        try {
            // Lê todas as linhas do arquivo, usando UTF-8
            Files.lines(Paths.get(caminhoArquivo), StandardCharsets.UTF_8)
                    .skip(1) // Pula o cabeçalho
                    .map(linha -> linha.split(";", -1)) // Divide cada linha por ponto e vírgula, mantendo campos vazios
                    .filter(colunas -> colunas.length >= 12) // Garante que há pelo menos 12 colunas
                    .forEach(colunas -> {
                        try {
                            ServidorInfo info = new ServidorInfo();

                            info.administradora = colunas[0].trim();
                            info.host           = colunas[1].trim();
                            info.porta          = Integer.parseInt(colunas[2].trim());
                            info.protocolo      = colunas[3].trim();
                            info.usuario        = colunas[4].trim();
                            info.senha          = colunas[5].trim();
                            info.pastaOrigem    = colunas[6].trim();
                            info.pastaDestino   = colunas[7].trim();

                            // Coluna 8: Nome da pasta de movimentação (ex: "Enviados")
                            // Se vazia, significa que não deve mover
                            info.pastaMover     = colunas[8].trim();

                            info.fingerprint    = colunas[9].trim();
                            info.hostKey        = colunas[10].trim();

                            // Coluna 12: Ativo (esperado: "Sim" ou "Não")
                            info.ativo          = colunas[11].trim().equalsIgnoreCase("sim");

                            servidores.add(info);

                        } catch (Exception e) {
                            System.err.println("Erro ao processar linha: " + String.join(";", colunas));
                            e.printStackTrace();
                        }
                    });

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo CSV: " + caminhoArquivo);
            e.printStackTrace();
        }

        return servidores;
    }
}
