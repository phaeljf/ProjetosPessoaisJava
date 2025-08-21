package br.com.caixaunica;
import java.nio.file.Paths;

public class ServidorInfo {

    // Nome da administradora (ex: Alelo, Greencard)
    public String administradora;

    // Endereço do servidor FTP ou SFTP
    public String host;

    // Porta de conexão (ex: 21 para FTP, 22 para SFTP)
    public int porta;

    // Protocolo usado: "ftp" ou "sftp"
    public String protocolo;

    // Usuário para login
    public String usuario;

    // Senha do usuário
    public String senha;

    // Caminho da pasta remota de onde baixar os arquivos
    public String pastaOrigem;

    // Nome da subpasta local (será usado em C:\Clientes\{pastaDestino})
    public String pastaDestino;

    // Nome da subpasta remota para onde os arquivos devem ser movidos após o download
    // Exemplo: "Enviados" ou "Recebidos". Se estiver vazio, não deve mover nada.
    public String pastaMover;

    // Para conexões SFTP: fingerprint esperada do host (verificação de segurança)
    public String fingerprint;

    // Para conexões SFTP: chave do host se necessário
    public String hostKey;

    // Indica se esta entrada da planilha está ativa ("Sim" ou "Não")
    public boolean ativo;

    // Caminho completo da pasta local onde os arquivos serão salvos
    public String getPastaDestinoCompleta() {
        // Se já começa com C:\, apenas retorna
        if (pastaDestino.toLowerCase().startsWith("c:\\")) {
            return pastaDestino;
        }
        return Paths.get("C:", pastaDestino).toString();
    }


    // Retorna true se o protocolo for SFTP (usado para lógica condicional no Main)
    public boolean isSftp() {
        return protocolo != null && protocolo.trim().equalsIgnoreCase("sftp");
    }

    @Override
    public String toString() {
        return "[ServidorInfo] " + protocolo + "://" + usuario + "@" + host + ":" + porta + " /" + pastaOrigem;
    }
}
