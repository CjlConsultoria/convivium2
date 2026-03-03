package com.convivium.module.billing.controller;

import com.convivium.config.MockMvcSecurityConfig;
import com.convivium.module.billing.service.PlatformInvoiceService;
import com.convivium.module.billing.service.StripeService;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Plan;
import com.convivium.module.condominium.repository.CondominiumRepository;
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
import java.util.Map;
import java.util.Optional;

import static com.convivium.security.TestSecurityUtils.withUserPrincipal;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(MockMvcSecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private StripeService stripeService;
    @MockBean
    private PlatformInvoiceService platformInvoiceService;
    @MockBean
    private CondominiumRepository condominiumRepository;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserRepository userRepository;

    @Test
    void createCheckoutSession_withPlanId_returnsUrl() throws Exception {
        when(stripeService.createCheckoutSession(1L, 2L, true)).thenReturn("https://checkout.stripe.com/session123");
        Map<String, Object> body = Map.of("planId", 2);
        mockMvc.perform(post("/api/v1/condos/1/payment/checkout-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").value("https://checkout.stripe.com/session123"));
    }

    @Test
    void createCheckoutSession_withoutPlanId_usesCondo() throws Exception {
        Plan plan = new Plan();
        plan.setId(3L);
        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setPlan(plan);
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(condo));
        when(stripeService.createCheckoutSession(1L, 3L, true)).thenReturn("https://checkout.stripe.com/session456");

        mockMvc.perform(post("/api/v1/condos/1/payment/checkout-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("https://checkout.stripe.com/session456"));
    }

    @Test
    void createCheckoutSession_noPlan_returnsBadRequest() throws Exception {
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(new Condominium()));

        mockMvc.perform(post("/api/v1/condos/1/payment/checkout-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(withUserPrincipal())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void listInvoices_returnsOk() throws Exception {
        PlatformInvoiceService.PlatformInvoiceDto dto = new PlatformInvoiceService.PlatformInvoiceDto(
                1L, "2024-01", "Jan/2024", 9900, "PAID", "2024-01-15T10:00:00Z", "2024-01-01T00:00:00Z"
        );
        when(platformInvoiceService.listByCondominium(1L, 24)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/condos/1/payment/invoices").with(withUserPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].referenceMonth").value("2024-01"));
    }
}
