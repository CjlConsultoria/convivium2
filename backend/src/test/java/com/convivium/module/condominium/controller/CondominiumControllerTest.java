package com.convivium.module.condominium.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.module.condominium.dto.CondominiumCreateRequest;
import com.convivium.module.condominium.dto.CondominiumResponse;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.service.CondominiumService;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.TestSecurityUtils;
import com.convivium.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.convivium.security.TestSecurityUtils.withUserPrincipal;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CondominiumController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class CondominiumControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CondominiumService condominiumService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CondominiumRepository condominiumRepository;

    @Test
    void listAll_returnsOk() throws Exception {
        CondominiumResponse cr = new CondominiumResponse(
            1L, "Condo", "condo",
            null, null, null, null, null, null, null, null, null, null, null,  // cnpj..logoUrl (11)
            null, null, null,  // planId, planName, planPriceCents (3)
            "ACTIVE", null
        );
        when(condominiumService.listAll(any())).thenReturn(new PageImpl<>(List.of(cr)));
        mockMvc.perform(get("/api/v1/admin/condominiums").with(withUserPrincipal(1L, 1L, "PLATFORM_ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void create_returnsCreated() throws Exception {
        CondominiumResponse cr = new CondominiumResponse(
            1L, "Condo", "condo",
            null, null, null, null, null, null, null, null, null, null, null,  // cnpj..logoUrl (11)
            null, null, null,  // planId, planName, planPriceCents (3)
            "ACTIVE", null
        );
        when(condominiumService.create(any())).thenReturn(cr);
        CondominiumCreateRequest req = new CondominiumCreateRequest("Condo", null, null, null, null, null, null, null, null, null, null);
        mockMvc.perform(post("/api/v1/admin/condominiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(withUserPrincipal(1L, 1L, "PLATFORM_ADMIN"))
                        .with(csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getById_returnsOk() throws Exception {
        CondominiumResponse cr = new CondominiumResponse(
            1L, "Condo", "condo",
            null, null, null, null, null, null, null, null, null, null, null,  // cnpj..logoUrl (11)
            null, null, null,  // planId, planName, planPriceCents (3)
            "ACTIVE", null
        );
        when(condominiumService.getById(1L)).thenReturn(cr);
        mockMvc.perform(get("/api/v1/admin/condominiums/1").with(withUserPrincipal(1L, 1L, "PLATFORM_ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("Condo"));
    }
}
