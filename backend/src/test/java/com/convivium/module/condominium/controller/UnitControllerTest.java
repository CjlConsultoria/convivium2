package com.convivium.module.condominium.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.module.condominium.dto.UnitCreateRequest;
import com.convivium.module.condominium.dto.UnitResponse;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.service.CondominiumService;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.convivium.security.TestSecurityUtils.withUserPrincipal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UnitController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class UnitControllerTest {

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
    void listUnits_returnsOk() throws Exception {
        UnitResponse ur = new UnitResponse(1L, 1L, "Bloco A", "101", 1, "APARTMENT", BigDecimal.valueOf(50), false, List.of());
        when(condominiumService.listUnits(1L)).thenReturn(List.of(ur));
        mockMvc.perform(get("/api/v1/condos/1/units").with(withUserPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].identifier").value("101"));
    }

    @Test
    void createUnit_returnsCreated() throws Exception {
        UnitResponse ur = new UnitResponse(1L, 1L, "Bloco A", "102", 1, "APARTMENT", BigDecimal.valueOf(60), false, List.of());
        when(condominiumService.createUnit(eq(1L), any(UnitCreateRequest.class))).thenReturn(ur);
        UnitCreateRequest req = new UnitCreateRequest(1L, "102", 1, "APARTMENT", BigDecimal.valueOf(60));
        mockMvc.perform(post("/api/v1/condos/1/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.identifier").value("102"));
    }

    @Test
    void deleteUnit_returnsOk() throws Exception {
        doNothing().when(condominiumService).deleteUnit(1L, 5L);
        mockMvc.perform(delete("/api/v1/condos/1/units/5")
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
