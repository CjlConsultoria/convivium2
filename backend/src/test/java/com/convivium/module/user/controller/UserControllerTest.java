package com.convivium.module.user.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.user.dto.UserCreateRequest;
import com.convivium.module.user.dto.UserResponse;
import com.convivium.module.user.dto.UserUpdateRequest;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.module.user.service.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static com.convivium.security.TestSecurityUtils.withUserPrincipal;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CondominiumRepository condominiumRepository;

    @Test
    void listUsers_returnsOk() throws Exception {
        UserResponse ur = new UserResponse(1L, "uuid", "a@b.com", "User", null, null, null, true, "MORADOR", "ACTIVE", null, null, null);
        when(userService.listUsers(eq(1L), any())).thenReturn(new PageImpl<>(List.of(ur)));
        mockMvc.perform(get("/api/v1/condos/1/users").with(withUserPrincipal()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createUser_returnsCreated() throws Exception {
        UserResponse ur = new UserResponse(1L, "uuid", "a@b.com", "User", null, null, null, true, "MORADOR", "ACTIVE", null, null, null);
        when(userService.createUser(eq(1L), any())).thenReturn(ur);
        UserCreateRequest req = new UserCreateRequest("User", "a@b.com", "senha123", null, null, "MORADOR", null);
        mockMvc.perform(post("/api/v1/condos/1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUser_returnsOk() throws Exception {
        UserResponse ur = new UserResponse(1L, "uuid", "a@b.com", "User", null, null, null, true, "MORADOR", "ACTIVE", null, null, null);
        when(userService.getUser(1L, 1L)).thenReturn(ur);
        mockMvc.perform(get("/api/v1/condos/1/users/1").with(withUserPrincipal()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.email").value("a@b.com"));
    }

    @Test
    void deleteUser_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/condos/1/users/1").with(withUserPrincipal()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void getPendingApprovals_returnsOk() throws Exception {
        when(userService.getPendingApprovals(1L)).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/condos/1/users/pending").with(withUserPrincipal()))
                .andExpect(status().isOk());
    }
}
