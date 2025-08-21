package br.com.baixa;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//Classe utilitária para ler e converter o arquivo CSV em objetos ServidorInfo.
public class CSVReader {

    public static List<ServidorInfo> lerCSV(String caminhoArquivo) {
        List<ServidorInfo> servidores = new ArrayList<>();

        try {
            // Lê o arquivo linha por linha, usando UTF-8
            Files.lines(Paths.get(caminhoArquivo), StandardCharsets.UTF_8)
                    .skip(1) // Pula o cabeçalho
                    .map(linha -> linha.split(";", -1)) // Divide por ponto e vírgula, mantendo campos vazios
                    .filter(colunas -> colunas.length >= 8) // Garante que há ao menos 8 colunas
                    .forEach(colunas -> {
                        // Cria e preenche o objeto
                        ServidorInfo info = new ServidorInfo();
                        info.host = colunas[0].trim();
                        info.usuario = colunas[1].trim();
                        info.senha = colunas[2].trim();
                        info.pastaOrigem = colunas[3].trim();
                        info.pastaDestino = colunas[4].trim();
                        info.fingerprint = colunas[6].trim();
                        info.hostkey = colunas[7].trim();
                        servidores.add(info);
                    });

        } catch (IOException e) {
            System.err.println("❌ Erro ao ler o arquivo CSV: " + e.getMessage());
        }

        return servidores;
    }
}
