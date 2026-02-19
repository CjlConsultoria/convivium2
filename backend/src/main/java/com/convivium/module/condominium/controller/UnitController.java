package com.convivium.module.condominium.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.condominium.dto.UnitCreateRequest;
import com.convivium.module.condominium.dto.UnitResponse;
import com.convivium.module.condominium.service.CondominiumService;
import com.convivium.security.CurrentUser;
import com.convivium.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condos/{condoId}/units")
@RequiredArgsConstructor
public class UnitController {

    private final CondominiumService condominiumService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<UnitResponse>>> listUnits(
            @PathVariable Long condoId,
            @CurrentUser UserPrincipal currentUser) {

        List<UnitResponse> units = condominiumService.listUnits(condoId);
        return ResponseEntity.ok(ApiResponse.ok(units));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<UnitResponse>> createUnit(
            @PathVariable Long condoId,
            @Valid @RequestBody UnitCreateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        UnitResponse response = condominiumService.createUnit(condoId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Unidade criada com sucesso"));
    }

    @DeleteMapping("/{unitId}")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(
            @PathVariable Long condoId,
            @PathVariable Long unitId,
            @CurrentUser UserPrincipal currentUser) {

        condominiumService.deleteUnit(condoId, unitId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Unidade excluida."));
    }
}
