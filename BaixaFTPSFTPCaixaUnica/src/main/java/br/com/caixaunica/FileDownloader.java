package br.com.caixaunica;

import java.util.Map;

public interface FileDownloader {

    // Conecta ao servidor (FTP ou SFTP)
    boolean conectar();

    // Conta quantos arquivos estão disponíveis na pasta remota
    int contarArquivos();

    // Baixa os arquivos do servidor para a pasta local
    int baixarArquivos();

    // Move os arquivos remotos para uma subpasta (ex: Enviados), se configurado
    boolean moverArquivos();

    // Encerra a conexão com o servidor
    void fecharConexao();

    // Lista os arquivos no servidor remoto com nome e tamanho
    Map<String, Long> listarArquivosServidor();

    // Lista os arquivos locais baixados com nome e tamanho (usado para validação)
    Map<String, Long> listarArquivosLocais(String pastaDestino);
}
