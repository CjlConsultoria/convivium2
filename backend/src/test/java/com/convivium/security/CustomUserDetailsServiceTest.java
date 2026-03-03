package com.convivium.security;

import com.convivium.module.user.entity.User;
import com.convivium.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_throwsWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("unknown@test.com"));
    }

    @Test
    void loadUserByUsername_returnsUserPrincipal() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@b.com");
        user.setName("User");
        user.setPasswordHash("hash");
        user.setActive(true);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("a@b.com");

        assertNotNull(details);
        assertTrue(details instanceof UserPrincipal);
        assertEquals("a@b.com", details.getUsername());
        assertEquals("hash", details.getPassword());
    }
}
