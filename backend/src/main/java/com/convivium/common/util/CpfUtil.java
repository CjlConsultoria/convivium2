package com.convivium.common.util;

/**
 * Normaliza CPF para apenas digitos (11 caracteres), para armazenamento e comparacao unificada.
 */
public final class CpfUtil {

    private CpfUtil() {
    }

    public static String normalize(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }
        String digits = cpf.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }
}
