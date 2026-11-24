package br.com.gmhsupport.util;

// Validação de CPF pelos 2 dígitos verificadores (regra da Receita Federal)
public final class Cpf {

    private Cpf() {
    }

    // "123.456.789-09" -> "12345678909"
    public static String somenteNumeros(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    public static boolean valido(String cpf) {
        String n = somenteNumeros(cpf);
        if (n.length() != 11) return false;
        // 000.000.000-00, 111.111.111-11... passam na conta, mas não existem
        if (n.chars().distinct().count() == 1) return false;
        return digito(n, 9) == n.charAt(9) - '0' && digito(n, 10) == n.charAt(10) - '0';
    }

    private static int digito(String n, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (n.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    // "12345678909" -> "***.456.789-**" (para mostrar sem expor tudo)
    public static String mascarado(String cpf) {
        String n = somenteNumeros(cpf);
        if (n.length() != 11) return null;
        return "***." + n.substring(3, 6) + "." + n.substring(6, 9) + "-**";
    }
}
