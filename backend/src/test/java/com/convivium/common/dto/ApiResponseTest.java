package com.convivium.common.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void ok_withData() {
        ApiResponse<String> response = ApiResponse.ok("data");
        assertTrue(response.isSuccess());
        assertEquals("data", response.getData());
        assertNull(response.getMessage());
    }

    @Test
    void ok_withDataAndMessage() {
        ApiResponse<String> response = ApiResponse.ok("data", "Sucesso");
        assertTrue(response.isSuccess());
        assertEquals("data", response.getData());
        assertEquals("Sucesso", response.getMessage());
    }

    @Test
    void error_withMessage() {
        ApiResponse<Void> response = ApiResponse.error("Erro");
        assertFalse(response.isSuccess());
        assertEquals("Erro", response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    void error_withMessageAndCode() {
        ApiResponse<Void> response = ApiResponse.error("Erro", "INVALID_CREDENTIALS");
        assertFalse(response.isSuccess());
        assertEquals("INVALID_CREDENTIALS", response.getErrorCode());
    }

    @Test
    void validationError() {
        var errors = List.of(new ApiResponse.FieldError("email", "inválido"));
        ApiResponse<Void> response = ApiResponse.validationError(errors);
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getErrorCode());
        assertEquals(1, response.getErrors().size());
        assertEquals("email", response.getErrors().get(0).field());
    }
}
