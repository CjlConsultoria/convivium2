package com.convivium.module.condominium.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.condominium.dto.ApplyStructureRequest;
import com.convivium.module.condominium.dto.GenerateStructureRequest;
import com.convivium.module.condominium.dto.StructurePreviewResponse;
import com.convivium.module.condominium.service.CondominiumService;
import com.convivium.security.CurrentUser;
import com.convivium.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/condos/{condoId}/structure")
@RequiredArgsConstructor
public class StructureController {

    private final CondominiumService condominiumService;

    /**
     * Preview: retorna a lista de blocos e unidades que seriam criados (não salva no banco).
     * O admin revisa, edita e depois chama POST /apply para salvar.
     */
    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<StructurePreviewResponse>> preview(
            @PathVariable Long condoId,
            @Valid @RequestBody GenerateStructureRequest request,
            @CurrentUser UserPrincipal currentUser) {

        StructurePreviewResponse response = condominiumService.previewStructure(condoId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Salva no banco a estrutura revisada (blocos e unidades confirmados pelo admin).
     */
    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> apply(
            @PathVariable Long condoId,
            @Valid @RequestBody ApplyStructureRequest request,
            @CurrentUser UserPrincipal currentUser) {

        condominiumService.applyStructure(condoId, request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Estrutura salva com sucesso."));
    }

    /**
     * Gera e salva direto (legado). Preferir preview + apply para permitir revisão.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> generate(
            @PathVariable Long condoId,
            @Valid @RequestBody GenerateStructureRequest request,
            @CurrentUser UserPrincipal currentUser) {

        condominiumService.generateStructure(condoId, request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Estrutura gerada com sucesso."));
    }
}
