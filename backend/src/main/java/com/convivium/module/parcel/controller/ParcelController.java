package com.convivium.module.parcel.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.common.dto.PageResponse;
import com.convivium.module.parcel.dto.ParcelCreateRequest;
import com.convivium.module.parcel.dto.ParcelDetailResponse;
import com.convivium.module.parcel.dto.ParcelListResponse;
import com.convivium.module.parcel.dto.ParcelVerifyRequest;
import com.convivium.module.parcel.entity.ParcelStatus;
import com.convivium.module.parcel.service.ParcelService;
import com.convivium.security.CurrentUser;
import com.convivium.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/condos/{condoId}/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;

    /** Lista todas as encomendas - apenas gestao (sindico, porteiro). Morador usa GET /my */
    @GetMapping
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ParcelListResponse>>> listParcels(
            @PathVariable Long condoId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {

        ParcelStatus parcelStatus = null;
        if (status != null && !status.isBlank()) {
            parcelStatus = ParcelStatus.valueOf(status.toUpperCase());
        }

        Page<ParcelListResponse> page = parcelService.listParcels(condoId, parcelStatus, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    /** Registrar recebimento de encomenda - apenas gestao (porteiro/sindico). Morador nao cria. */
    @PostMapping
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<ParcelListResponse>> createParcel(
            @PathVariable Long condoId,
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ParcelCreateRequest request) {

        ParcelListResponse response = parcelService.createParcel(condoId, currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Encomenda registrada com sucesso"));
    }

    @GetMapping("/{parcelId}")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO', 'MORADOR') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<ParcelDetailResponse>> getParcel(
            @PathVariable Long condoId,
            @PathVariable Long parcelId,
            @CurrentUser UserPrincipal currentUser) {

        ParcelDetailResponse response = parcelService.getParcel(condoId, parcelId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{parcelId}/generate-code")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateCodes(
            @PathVariable Long condoId,
            @PathVariable Long parcelId) {

        Map<String, String> codes = parcelService.generateCodes(condoId, parcelId);
        return ResponseEntity.ok(ApiResponse.ok(codes, "Codigos gerados com sucesso"));
    }

    @PostMapping("/{parcelId}/verify")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> verifyPickup(
            @PathVariable Long condoId,
            @PathVariable Long parcelId,
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ParcelVerifyRequest request) {

        parcelService.verifyPickup(condoId, parcelId, request, currentUser.getId());

        return ResponseEntity.ok(ApiResponse.ok(null, "Encomenda verificada e entregue com sucesso"));
    }

    @PostMapping("/{parcelId}/photos")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> uploadPhoto(
            @PathVariable Long condoId,
            @PathVariable Long parcelId,
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "photoType", defaultValue = "RECEIPT") String photoType) {

        parcelService.addPhoto(condoId, parcelId, file, photoType, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null, "Foto enviada com sucesso"));
    }

    /** Minhas encomendas (morador). Rota /my e /mine para compatibilidade com o front. */
    @GetMapping({ "/my", "/mine" })
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO', 'MORADOR') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ParcelListResponse>>> getMyParcels(
            @PathVariable Long condoId,
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {

        ParcelStatus parcelStatus = null;
        if (status != null && !status.isBlank()) {
            parcelStatus = ParcelStatus.valueOf(status.toUpperCase());
        }
        Page<ParcelListResponse> page = parcelService.getMyParcels(condoId, currentUser.getId(), parcelStatus, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }
}
