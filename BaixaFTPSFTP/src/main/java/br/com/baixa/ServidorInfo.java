package br.com.baixa;

//Representa as informações de um servidor obtido a partir da planilha CSV.
public class ServidorInfo {

    // Campos extraídos do CSV
    public String host;
    public String usuario;
    public String senha;
    public String pastaOrigem;
    public String pastaDestino;
    public String fingerprint;
    public String hostkey;

    //Verifica se o servidor é do tipo SFTP, com base no nome do host.
    public boolean isSFTP() {
        if (host == null) return false;
        String hostLower = host.toLowerCase();
        //return hostLower.contains("getnet") || hostLower.contains("cielo");
        return hostLower.contains("sftp");
    }

    // Verifica se o servidor é do tipo FTP, com base no nome do host.
    public boolean isFTP() {
        if (host == null) return false;
        if (isSFTP()) return false;  // PRIORIDADE: Se for SFTP, NÃO é FTP!
        String hostLower = host.toLowerCase();
        //return hostLower.contains("vegascard") || hostLower.contains("greencard") || hostLower.contains("senff");
        return hostLower.contains("ftp");
    }

    //Representação textual do servidor para logs e exibições.
    @Override
    public String toString() {
        String tipo = isSFTP() ? "[SFTP]" : isFTP() ? "[FTP]" : "[?]";
        return tipo + " " + host + " - " + usuario;
    }
}
