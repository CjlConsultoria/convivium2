package com.convivium.security;

import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.repository.CondominiumRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private static final Pattern CONDO_PATH_PATTERN =
            Pattern.compile("/api/v1/condos/(\\d+)(/.*)?");

    /** Rotas que o sindico pode acessar quando bloqueado por PAYMENT */
    private static final Set<String> PAYMENT_ALLOWED_PREFIXES = Set.of(
            "/payment", "/payment/"
    );

    private final CondominiumRepository condominiumRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {

                Long condoIdFromPath = extractCondoIdFromPath(request.getRequestURI());

                if (condoIdFromPath != null) {
                    Optional<Condominium> condoOpt = condominiumRepository.findById(condoIdFromPath);

                    if (condoOpt.isPresent()) {
                        Condominium condo = condoOpt.get();
                        String subPath = extractSubPath(request.getRequestURI());

                        // 1) Bloqueio GENERAL: apenas PLATFORM_ADMIN acessa
                        if ("GENERAL".equals(condo.getBlockType())) {
                            if (!userPrincipal.isPlatformAdmin()) {
                                writeBlockedResponse(response,
                                        "Condominio bloqueado. Entre em contato com a plataforma.",
                                        "CONDOMINIUM_GENERAL_BLOCKED");
                                return;
                            }
                        }

                        // 2) Bloqueio PAYMENT: PLATFORM_ADMIN acessa tudo;
                        //    SINDICO acessa somente /payment/** e /api/v1/auth/me
                        if ("PAYMENT".equals(condo.getBlockType())) {
                            if (!userPrincipal.isPlatformAdmin()) {
                                boolean isSindico = userPrincipal.getAuthorities()
                                        .contains(new SimpleGrantedAuthority("ROLE_SINDICO"));

                                boolean isPaymentRoute = subPath != null && isPaymentAllowedRoute(subPath);
                                boolean isAuthMe = request.getRequestURI().equals("/api/v1/auth/me");

                                if (isSindico && (isPaymentRoute || isAuthMe)) {
                                    // Sindico pode acessar rotas de pagamento — continua
                                } else {
                                    writeBlockedResponse(response,
                                            "Condominio bloqueado por inadimplencia. Regularize o pagamento.",
                                            "CONDOMINIUM_PAYMENT_BLOCKED");
                                    return;
                                }
                            }
                        }

                        // 3) Manter bloqueio por status SUSPENDED (legacy)
                        if ("SUSPENDED".equals(condo.getStatus())) {
                            if (!userPrincipal.isPlatformAdmin()) {
                                writeBlockedResponse(response,
                                        "Condominio suspenso. Regularize sua situacao para acessar.",
                                        "CONDOMINIUM_SUSPENDED");
                                return;
                            }
                        }
                    }

                    // Platform admins can access any condominium
                    if (!userPrincipal.isPlatformAdmin()) {
                        // Validate that the condominium ID in the path matches the JWT claim
                        if (userPrincipal.getCondominiumId() == null
                                || !userPrincipal.getCondominiumId().equals(condoIdFromPath)) {
                            log.warn("User {} attempted to access condominium {} but is authorized for {}",
                                    userPrincipal.getUuid(), condoIdFromPath,
                                    userPrincipal.getCondominiumId());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "Access denied to this condominium");
                            return;
                        }
                    }
                    TenantContext.setCurrentTenantId(condoIdFromPath);
                } else if (userPrincipal.getCondominiumId() != null) {
                    // For non-condo-path requests, use the condominium from the JWT
                    TenantContext.setCurrentTenantId(userPrincipal.getCondominiumId());
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private Long extractCondoIdFromPath(String uri) {
        Matcher matcher = CONDO_PATH_PATTERN.matcher(uri);
        if (matcher.matches()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Invalid condominium ID in path: {}", uri);
                return null;
            }
        }
        return null;
    }

    private String extractSubPath(String uri) {
        Matcher matcher = CONDO_PATH_PATTERN.matcher(uri);
        if (matcher.matches()) {
            return matcher.group(2); // e.g., "/payment/checkout-session"
        }
        return null;
    }

    private boolean isPaymentAllowedRoute(String subPath) {
        for (String prefix : PAYMENT_ALLOWED_PREFIXES) {
            if (subPath.equals(prefix) || subPath.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void writeBlockedResponse(HttpServletResponse response, String message, String code)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"success\":false,\"message\":\"%s\",\"code\":\"%s\"}", message, code)
        );
    }
}
