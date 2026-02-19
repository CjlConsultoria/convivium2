package com.convivium.module.condominium.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.common.dto.PageResponse;
import com.convivium.module.condominium.dto.CondominiumCreateRequest;
import com.convivium.module.condominium.dto.CondominiumResponse;
import com.convivium.module.condominium.service.CondominiumService;
import com.convivium.security.CurrentUser;
import com.convivium.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/condominiums")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class CondominiumController {

    private final CondominiumService condominiumService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CondominiumResponse>>> listAll(
            @PageableDefault(size = 20) Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<CondominiumResponse> page = condominiumService.listAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CondominiumResponse>> create(
            @Valid @RequestBody CondominiumCreateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        CondominiumResponse response = condominiumService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Condominio criado com sucesso"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CondominiumResponse>> getById(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        CondominiumResponse response = condominiumService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CondominiumResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CondominiumCreateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        CondominiumResponse response = condominiumService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Condominio atualizado com sucesso"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @CurrentUser UserPrincipal currentUser) {

        String status = body.get("status");
        condominiumService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(null, "Status atualizado com sucesso"));
    }

    /** Associa um plano ao condominio. Envie { "planId": 1 } ou { "planId": null } para remover. */
    @PatchMapping("/{id}/plan")
    public ResponseEntity<ApiResponse<CondominiumResponse>> setPlan(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            @CurrentUser UserPrincipal currentUser) {

        Long planId = body.get("planId");
        CondominiumResponse response = condominiumService.setPlan(id, planId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Plano atualizado com sucesso"));
    }
}
