package com.convivium.module.condominium.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.module.condominium.dto.BuildingCreateRequest;
import com.convivium.module.condominium.dto.BuildingResponse;
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

import java.util.List;

import static com.convivium.security.TestSecurityUtils.withUserPrincipal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BuildingController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class BuildingControllerTest {

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
    void listBuildings_returnsOk() throws Exception {
        BuildingResponse br = new BuildingResponse(1L, "Bloco A", 5, "2024-01-01");
        when(condominiumService.listBuildings(1L)).thenReturn(List.of(br));
        mockMvc.perform(get("/api/v1/condos/1/buildings").with(withUserPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Bloco A"));
    }

    @Test
    void createBuilding_returnsCreated() throws Exception {
        BuildingResponse br = new BuildingResponse(1L, "Bloco B", 3, "2024-01-01");
        when(condominiumService.createBuilding(eq(1L), any(BuildingCreateRequest.class))).thenReturn(br);
        BuildingCreateRequest req = new BuildingCreateRequest("Bloco B", 3);
        mockMvc.perform(post("/api/v1/condos/1/buildings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Bloco B"));
    }

    @Test
    void deleteBuilding_returnsOk() throws Exception {
        doNothing().when(condominiumService).deleteBuilding(1L, 2L);
        mockMvc.perform(delete("/api/v1/condos/1/buildings/2")
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
