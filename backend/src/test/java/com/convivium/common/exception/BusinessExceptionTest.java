package com.convivium.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructorWithMessageAndCode() {
        BusinessException ex = new BusinessException("Erro", "INVALID_CREDENTIALS");
        assertEquals("Erro", ex.getMessage());
        assertEquals("INVALID_CREDENTIALS", ex.getErrorCode());
    }

    @Test
    void constructorWithMessageOnly() {
        BusinessException ex = new BusinessException("Erro");
        assertEquals("Erro", ex.getMessage());
        assertEquals("BUSINESS_ERROR", ex.getErrorCode());
    }
}
