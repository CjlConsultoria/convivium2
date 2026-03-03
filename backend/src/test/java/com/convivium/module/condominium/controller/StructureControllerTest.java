package com.convivium.module.condominium.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.module.condominium.dto.ApplyStructureRequest;
import com.convivium.module.condominium.dto.GenerateStructureRequest;
import com.convivium.module.condominium.dto.StructurePreviewResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StructureController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class StructureControllerTest {

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
    void preview_returnsOk() throws Exception {
        StructurePreviewResponse response = new StructurePreviewResponse(
                List.of(new StructurePreviewResponse.BuildingPreviewItem("Bloco A", 3)),
                List.of(new StructurePreviewResponse.UnitPreviewItem("Bloco A", 1, "101"))
        );
        when(condominiumService.previewStructure(eq(1L), any(GenerateStructureRequest.class))).thenReturn(response);
        GenerateStructureRequest req = new GenerateStructureRequest(1, 4, 3, "101", 1);
        mockMvc.perform(post("/api/v1/condos/1/structure/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.buildings[0].name").value("Bloco A"));
    }

    @Test
    void apply_returnsOk() throws Exception {
        doNothing().when(condominiumService).applyStructure(eq(1L), any(ApplyStructureRequest.class));
        ApplyStructureRequest req = new ApplyStructureRequest(
                List.of(new StructurePreviewResponse.BuildingPreviewItem("Bloco A", 3)),
                List.of(new StructurePreviewResponse.UnitPreviewItem("Bloco A", 1, "101"))
        );
        mockMvc.perform(post("/api/v1/condos/1/structure/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void generate_returnsOk() throws Exception {
        doNothing().when(condominiumService).generateStructure(eq(1L), any(GenerateStructureRequest.class));
        GenerateStructureRequest req = new GenerateStructureRequest(2, 4, 5, "101", 1);
        mockMvc.perform(post("/api/v1/condos/1/structure/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
