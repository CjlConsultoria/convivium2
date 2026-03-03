package com.convivium.module.parcel.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.parcel.dto.ParcelCreateRequest;
import com.convivium.module.parcel.dto.ParcelListResponse;
import com.convivium.module.parcel.service.ParcelService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ParcelController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class ParcelControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ParcelService parcelService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CondominiumRepository condominiumRepository;

    @Test
    void listParcels_returnsOk() throws Exception {
        ParcelListResponse plr = new ParcelListResponse(1L, "101", null, null, null, null, null, "RECEIVED", null, null);
        when(parcelService.listParcels(eq(1L), any(), any())).thenReturn(new PageImpl<>(List.of(plr)));
        mockMvc.perform(get("/api/v1/condos/1/parcels").with(withUserPrincipal()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createParcel_returnsCreated() throws Exception {
        ParcelListResponse plr = new ParcelListResponse(1L, "101", null, null, null, null, null, "RECEIVED", null, null);
        when(parcelService.createParcel(eq(1L), eq(1L), any(ParcelCreateRequest.class))).thenReturn(plr);
        ParcelCreateRequest req = new ParcelCreateRequest(10L, null, "Correios", null, null);
        mockMvc.perform(post("/api/v1/condos/1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getMyParcels_returnsOk() throws Exception {
        when(parcelService.getMyParcels(eq(1L), eq(1L), eq(null), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/condos/1/parcels/my").with(withUserPrincipal()))
                .andExpect(status().isOk());
    }
}
