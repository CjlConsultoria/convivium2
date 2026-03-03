package com.convivium.module.condominium.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.module.condominium.dto.CondominiumResponse;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.service.CondominiumService;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.convivium.security.TestSecurityUtils.withUserPrincipal;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CondoController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class CondoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CondominiumService condominiumService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CondominiumRepository condominiumRepository;

    @Test
    void getCondo_returnsOk() throws Exception {
        CondominiumResponse cr = new CondominiumResponse(
            1L, "Condo Test", "condo-test",
            null, null, null, null, null, null, null, null, null, null, null,
            null, null, null,
            "ACTIVE", null
        );
        when(condominiumService.getById(1L)).thenReturn(cr);
        mockMvc.perform(get("/api/v1/condos/1").with(withUserPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Condo Test"));
    }
}
