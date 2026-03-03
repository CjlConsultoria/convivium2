package com.convivium.module.condominium.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.module.condominium.entity.Plan;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.repository.PlanRepository;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.convivium.security.TestSecurityUtils.withUserPrincipal;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PlanController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PlanRepository planRepository;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CondominiumRepository condominiumRepository;

    @Test
    void listPlans_returnsOk() throws Exception {
        Plan plan = new Plan();
        plan.setId(1L);
        plan.setName("Basic");
        plan.setSlug("basic");
        plan.setPriceCents(9900);
        plan.setDescription("Plano basico");
        plan.setMaxUnits(50);
        plan.setMaxUsers(100);
        plan.setActive(true);

        when(planRepository.findByActiveTrueOrderByName()).thenReturn(List.of(plan));

        mockMvc.perform(get("/api/v1/admin/plans").with(withUserPrincipal(1L, 1L, "PLATFORM_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Basic"))
                .andExpect(jsonPath("$.data[0].priceCents").value(9900));
    }
}
