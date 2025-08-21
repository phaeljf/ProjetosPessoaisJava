package br.com.prosistema.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void info(String mensagem) {
        System.out.println("[INFO] " + sdf.format(new Date()) + " - " + mensagem);
    }

    public static void erro(String mensagem) {
        System.err.println("[ERRO] " + sdf.format(new Date()) + " - " + mensagem);
    }

    public static void alerta(String s) {
    }
}
