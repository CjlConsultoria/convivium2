package com.convivium.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfUtilTest {

    @Test
    void normalize_returnsDigitsOnly() {
        assertEquals("12345678901", CpfUtil.normalize("123.456.789-01"));
        assertEquals("12345678901", CpfUtil.normalize("12345678901"));
    }

    @Test
    void normalize_returnsNullForNull() {
        assertNull(CpfUtil.normalize(null));
    }

    @Test
    void normalize_returnsNullForBlank() {
        assertNull(CpfUtil.normalize(""));
        assertNull(CpfUtil.normalize("   "));
    }

    @Test
    void normalize_returnsNullWhenOnlyNonDigits() {
        assertNull(CpfUtil.normalize("...---"));
    }
}
