package com.convivium.module.dashboard.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.common.dto.PageResponse;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.dashboard.dto.DashboardStatsResponse;
import com.convivium.module.dashboard.dto.UnitActivityItemDto;
import com.convivium.module.dashboard.service.DashboardService;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.TestSecurityUtils;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DashboardController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CondominiumRepository condominiumRepository;

    @Test
    void getStats_returnsOk() throws Exception {
        DashboardStatsResponse stats = DashboardStatsResponse.builder().totalMoradores(10).denunciasAbertas(2).encomendasPendentes(3).reservasHoje(1).build();
        when(dashboardService.getStats(eq(1L), any())).thenReturn(stats);
        mockMvc.perform(get("/api/v1/condos/1/dashboard/stats").with(withUserPrincipal()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.totalMoradores").value(10));
    }

    @Test
    void getUnitActivity_returnsOk() throws Exception {
        PageResponse<UnitActivityItemDto> activity = new PageResponse<>();
        activity.setContent(List.of());
        activity.setPage(0);
        activity.setSize(10);
        activity.setTotalElements(0);
        activity.setTotalPages(0);
        activity.setLast(true);
        when(dashboardService.getUnitActivity(eq(1L), any(), eq(0), eq(10))).thenReturn(activity);
        mockMvc.perform(get("/api/v1/condos/1/dashboard/activity?page=0&size=10").with(withUserPrincipal()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
    }
}
