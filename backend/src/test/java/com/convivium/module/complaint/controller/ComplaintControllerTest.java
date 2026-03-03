package com.convivium.module.complaint.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.module.complaint.dto.ComplaintCreateRequest;
import com.convivium.module.complaint.dto.ComplaintListResponse;
import com.convivium.module.complaint.service.ComplaintService;
import com.convivium.module.condominium.repository.CondominiumRepository;
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

@WebMvcTest(controllers = ComplaintController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ComplaintService complaintService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CondominiumRepository condominiumRepository;

    @Test
    void listComplaints_returnsOk() throws Exception {
        ComplaintListResponse clr = new ComplaintListResponse(1L, null, false, "NOISE", "Titulo", "OPEN", "MEDIUM", null, null, 0);
        when(complaintService.listComplaints(eq(1L), eq(null), any())).thenReturn(new PageImpl<>(List.of(clr)));
        mockMvc.perform(get("/api/v1/condos/1/complaints").with(withUserPrincipal()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createComplaint_returnsCreated() throws Exception {
        ComplaintListResponse clr = new ComplaintListResponse(1L, null, false, "NOISE", "Titulo", "OPEN", "MEDIUM", null, null, 0);
        when(complaintService.createComplaint(eq(1L), eq(1L), any(ComplaintCreateRequest.class))).thenReturn(clr);
        ComplaintCreateRequest req = new ComplaintCreateRequest("NOISE", "Titulo", "Descricao", false, null, null);
        mockMvc.perform(post("/api/v1/condos/1/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getMyComplaints_returnsOk() throws Exception {
        when(complaintService.getMyComplaints(eq(1L), eq(1L), eq(null), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/condos/1/complaints/my").with(withUserPrincipal()))
                .andExpect(status().isOk());
    }
}
