package com.convivium.common.exception;

import com.convivium.common.dto.ApiResponse;
import com.convivium.integration.google.GoogleIdTokenVerifier.InvalidGoogleTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario", 1L);
        ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
    }

    @Test
    void handleBusinessException_returns422() {
        BusinessException ex = new BusinessException("Erro", "CODE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CODE", response.getBody().getErrorCode());
    }

    @Test
    void handleAccessDenied_returns403() {
        AccessDeniedException ex = new AccessDeniedException("Denied");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().getErrorCode());
    }

    @Test
    void handleAuthentication_returns401() {
        AuthenticationException ex = new org.springframework.security.authentication.BadCredentialsException("bad");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAuthentication(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleGenericException_returns500() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(new RuntimeException("error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
    }

    @Test
    void handleInvalidGoogleToken_returns401() {
        InvalidGoogleTokenException ex = new InvalidGoogleTokenException("Token invalido");
        ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidGoogleToken(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_GOOGLE_TOKEN", response.getBody().getErrorCode());
    }

    @Test
    void handleValidation_returns400() {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "obj");
        errors.addError(new FieldError("obj", "email", "Email invalido"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, errors);
        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
    }
}
