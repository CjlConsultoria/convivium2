package com.convivium.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6");
        props.setAccessTokenExpiration(900_000L);
        props.setRefreshTokenExpiration(604_800_000L);
        jwtTokenProvider = new JwtTokenProvider(props);
        jwtTokenProvider.init();
    }

    @Test
    void generateAccessToken_returnsValidToken() {
        String token = jwtTokenProvider.generateAccessToken(
                "user-uuid",
                "user@example.com",
                1L,
                List.of("SINDICO"),
                List.of()
        );
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateRefreshToken_returnsValidToken() {
        String token = jwtTokenProvider.generateRefreshToken("user-uuid");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        String token = jwtTokenProvider.generateAccessToken(
                "user-uuid",
                "user@example.com",
                null,
                List.of(),
                List.of()
        );
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_returnsFalseForInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }

    @Test
    void getSubjectFromToken_returnsUuid() {
        String token = jwtTokenProvider.generateAccessToken(
                "user-uuid-123",
                "user@example.com",
                null,
                List.of(),
                List.of()
        );
        assertEquals("user-uuid-123", jwtTokenProvider.getSubject(token));
    }
}
