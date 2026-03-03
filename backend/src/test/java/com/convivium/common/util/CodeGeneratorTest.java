package com.convivium.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorTest {

    @Test
    void generateNumericCode_returnsCorrectLength() {
        String code = CodeGenerator.generateNumericCode(6);
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d+"));
    }

    @Test
    void generateNumericCode_throwsWhenLengthZero() {
        assertThrows(IllegalArgumentException.class, () -> CodeGenerator.generateNumericCode(0));
    }

    @Test
    void generateNumericCode_throwsWhenLengthNegative() {
        assertThrows(IllegalArgumentException.class, () -> CodeGenerator.generateNumericCode(-1));
    }

    @Test
    void generateAlphanumericCode_returnsCorrectLength() {
        String code = CodeGenerator.generateAlphanumericCode(10);
        assertNotNull(code);
        assertEquals(10, code.length());
        assertTrue(code.matches("[0-9A-Za-z]+"));
    }

    @Test
    void generateAlphanumericCode_throwsWhenLengthZero() {
        assertThrows(IllegalArgumentException.class, () -> CodeGenerator.generateAlphanumericCode(0));
    }

    @Test
    void generateUUID_returnsValidUUID() {
        String uuid = CodeGenerator.generateUUID();
        assertNotNull(uuid);
        assertDoesNotThrow(() -> java.util.UUID.fromString(uuid));
    }
}
