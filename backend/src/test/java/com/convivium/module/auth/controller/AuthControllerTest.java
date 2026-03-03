package com.convivium.module.auth.controller;

import com.convivium.module.auth.dto.LoginRequest;
import com.convivium.module.auth.dto.LoginResponse;
import com.convivium.module.auth.dto.UserInfoResponse;
import com.convivium.module.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void login_returnsOkWhenValid() throws Exception {
        LoginResponse response = new LoginResponse(
                "accessToken",
                "refreshToken",
                900L,
                new UserInfoResponse(1L, "uuid", "a@b.com", "User", null, null, false, List.of())
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("a@b.com", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
    }

    @Test
    void listCondominiums_returnsOk() throws Exception {
        when(authService.listCondominiumsForRegistration()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/auth/condominiums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
