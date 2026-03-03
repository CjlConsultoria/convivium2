package com.convivium.security;

import com.convivium.module.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    @Test
    void fromUser_createsPrincipalWithRolesAndPermissions() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@b.com");
        user.setName("User");
        user.setPasswordHash("hash");
        user.setActive(true);
        user.setPlatformAdmin(false);

        UserPrincipal principal = UserPrincipal.fromUser(user, 10L,
                List.of("SINDICO", "MORADOR"),
                List.of("MANAGE_PARCELS"));

        assertNotNull(principal);
        assertEquals(1L, principal.getId());
        assertEquals("a@b.com", principal.getEmail());
        assertEquals("User", principal.getName());
        assertEquals("hash", principal.getPassword());
        assertEquals(10L, principal.getCondominiumId());
        assertFalse(principal.isPlatformAdmin());
        assertTrue(principal.isActive());
        assertTrue(principal.getAuthorities().stream().anyMatch(a -> "ROLE_SINDICO".equals(a.getAuthority())));
        assertTrue(principal.getAuthorities().stream().anyMatch(a -> "ROLE_MORADOR".equals(a.getAuthority())));
        assertTrue(principal.getAuthorities().stream().anyMatch(a -> "MANAGE_PARCELS".equals(a.getAuthority())));
    }

    @Test
    void fromUser_withNullPassword_usesNoop() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@b.com");
        user.setName("User");
        user.setPasswordHash(null);
        user.setActive(true);

        UserPrincipal principal = UserPrincipal.fromUser(user, null, List.of(), List.of());
        assertEquals("{noop}__no_password__", principal.getPassword());
    }

    @Test
    void getters_returnCorrectValues() {
        UserPrincipal principal = UserPrincipal.builder()
                .id(1L)
                .uuid("uuid")
                .email("a@b.com")
                .password("pwd")
                .name("User")
                .condominiumId(1L)
                .isPlatformAdmin(false)
                .active(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_SINDICO")))
                .build();

        assertEquals(1L, principal.getId());
        assertEquals("uuid", principal.getUuid());
        assertEquals("a@b.com", principal.getUsername());
        assertEquals("pwd", principal.getPassword());
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
        assertTrue(principal.isEnabled());
    }
}
