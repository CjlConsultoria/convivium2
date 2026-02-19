package com.convivium.common.util;

import java.security.SecureRandom;
import java.util.UUID;

public final class CodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String NUMERIC_CHARS = "0123456789";
    private static final String ALPHANUMERIC_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private CodeGenerator() {
        // Utility class - prevent instantiation
    }

    public static String generateNumericCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("O comprimento deve ser maior que zero");
        }
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(NUMERIC_CHARS.charAt(RANDOM.nextInt(NUMERIC_CHARS.length())));
        }
        return sb.toString();
    }

    public static String generateAlphanumericCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("O comprimento deve ser maior que zero");
        }
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC_CHARS.charAt(RANDOM.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return sb.toString();
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
