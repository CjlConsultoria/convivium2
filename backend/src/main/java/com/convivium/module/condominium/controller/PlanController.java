package com.convivium.module.condominium.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.condominium.dto.PlanResponse;
import com.convivium.module.condominium.entity.Plan;
import com.convivium.module.condominium.repository.PlanRepository;
import com.convivium.security.CurrentUser;
import com.convivium.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class PlanController {

    private final PlanRepository planRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanResponse>>> listPlans(@CurrentUser UserPrincipal currentUser) {
        List<PlanResponse> list = planRepository.findByActiveTrueOrderByName().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    private PlanResponse toResponse(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getSlug(),
                plan.getPriceCents(),
                plan.getDescription(),
                plan.getMaxUnits(),
                plan.getMaxUsers(),
                plan.getActive()
        );
    }
}
