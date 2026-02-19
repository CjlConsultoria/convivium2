package com.convivium.security.jwt;

import com.convivium.module.user.entity.User;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                UserPrincipal userPrincipal = createUserPrincipalFromToken(jwt);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                userPrincipal.getAuthorities()
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private UserPrincipal createUserPrincipalFromToken(String token) {
        String uuid = jwtTokenProvider.getSubject(token);
        String email = jwtTokenProvider.getEmail(token);
        Long condominiumId = jwtTokenProvider.getCondominiumId(token);
        List<String> roles = jwtTokenProvider.getRoles(token);
        List<String> permissions = jwtTokenProvider.getPermissions(token);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (roles != null) {
            roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }

        if (permissions != null) {
            permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        boolean isPlatformAdmin = roles != null && roles.contains("PLATFORM_ADMIN");

        // Resolve database ID from UUID
        Long userId = null;
        String name = null;
        try {
            User user = userRepository.findByUuid(UUID.fromString(uuid)).orElse(null);
            if (user != null) {
                userId = user.getId();
                name = user.getName();
            }
        } catch (Exception ex) {
            log.warn("Could not resolve user ID from UUID {}: {}", uuid, ex.getMessage());
        }

        return UserPrincipal.builder()
                .id(userId)
                .uuid(uuid)
                .email(email)
                .name(name)
                .condominiumId(condominiumId)
                .isPlatformAdmin(isPlatformAdmin)
                .active(true)
                .authorities(new ArrayList<>(authorities))
                .build();
    }
}
