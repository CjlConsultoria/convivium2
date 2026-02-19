package com.convivium.module.condominium.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.condominium.dto.BuildingCreateRequest;
import com.convivium.module.condominium.dto.BuildingResponse;
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
@RequestMapping("/api/v1/condos/{condoId}/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final CondominiumService condominiumService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<BuildingResponse>>> listBuildings(
            @PathVariable Long condoId,
            @CurrentUser UserPrincipal currentUser) {

        List<BuildingResponse> buildings = condominiumService.listBuildings(condoId);
        return ResponseEntity.ok(ApiResponse.ok(buildings));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<BuildingResponse>> createBuilding(
            @PathVariable Long condoId,
            @Valid @RequestBody BuildingCreateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        BuildingResponse response = condominiumService.createBuilding(condoId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Bloco criado com sucesso"));
    }

    @DeleteMapping("/{buildingId}")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBuilding(
            @PathVariable Long condoId,
            @PathVariable Long buildingId,
            @CurrentUser UserPrincipal currentUser) {

        condominiumService.deleteBuilding(condoId, buildingId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Bloco excluido."));
    }
}
