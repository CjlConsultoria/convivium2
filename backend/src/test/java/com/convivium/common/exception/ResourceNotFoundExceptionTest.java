package com.convivium.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void constructorSetsMessageAndFields() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario", 123L);
        assertEquals("Usuario nao encontrado com id: 123", ex.getMessage());
        assertEquals("Usuario", ex.getEntityName());
        assertEquals(123L, ex.getId());
    }
}
