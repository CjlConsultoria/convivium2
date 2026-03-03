package com.convivium.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @Test
    void setAndGetCurrentTenantId() {
        TenantContext.clear();
        assertNull(TenantContext.getCurrentTenantId());

        TenantContext.setCurrentTenantId(1L);
        assertEquals(1L, TenantContext.getCurrentTenantId());

        TenantContext.setCurrentTenantId(2L);
        assertEquals(2L, TenantContext.getCurrentTenantId());

        TenantContext.clear();
        assertNull(TenantContext.getCurrentTenantId());
    }

    @Test
    void clearRemovesTenantId() {
        TenantContext.setCurrentTenantId(99L);
        TenantContext.clear();
        assertNull(TenantContext.getCurrentTenantId());
    }
}
