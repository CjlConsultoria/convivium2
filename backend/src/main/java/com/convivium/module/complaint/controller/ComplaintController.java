package com.convivium.module.complaint.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.common.dto.PageResponse;
import com.convivium.module.complaint.dto.ComplaintCreateRequest;
import com.convivium.module.complaint.dto.ComplaintDetailResponse;
import com.convivium.module.complaint.dto.ComplaintListResponse;
import com.convivium.module.complaint.dto.ComplaintResponseCreateRequest;
import com.convivium.module.complaint.dto.ComplaintResponseDto;
import com.convivium.module.complaint.dto.ComplaintStatusUpdateRequest;
import com.convivium.module.complaint.entity.ComplaintStatus;
import com.convivium.module.complaint.service.ComplaintService;
import com.convivium.security.CurrentUser;
import com.convivium.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/condos/{condoId}/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    /** Lista todas as denuncias do condominio - apenas sindico, sub, porteiro, admin. Morador usa GET /my */
    @GetMapping
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ComplaintListResponse>>> listComplaints(
            @PathVariable Long condoId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {

        ComplaintStatus complaintStatus = null;
        if (status != null && !status.isBlank()) {
            complaintStatus = ComplaintStatus.valueOf(status.toUpperCase());
        }

        Page<ComplaintListResponse> page = complaintService.listComplaints(condoId, complaintStatus, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    /** Morador e gestao podem criar denuncia */
    @PostMapping
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO', 'MORADOR') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintListResponse>> createComplaint(
            @PathVariable Long condoId,
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ComplaintCreateRequest request) {

        ComplaintListResponse response = complaintService.createComplaint(condoId, currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Reclamacao criada com sucesso"));
    }

    @GetMapping("/{complaintId}")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO', 'MORADOR') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> getComplaint(
            @PathVariable Long condoId,
            @PathVariable Long complaintId,
            @CurrentUser UserPrincipal currentUser) {

        boolean canViewInternal = hasAnyAuthority(currentUser, "ROLE_SINDICO", "ROLE_SUB_SINDICO",
                "ROLE_PORTEIRO", "ROLE_PLATFORM_ADMIN");
        boolean isMoradorOnly = !canViewInternal;

        ComplaintDetailResponse response = complaintService.getComplaintFiltered(
                condoId, complaintId, canViewInternal, currentUser.getId(), isMoradorOnly);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{complaintId}/responses")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponseDto>> addResponse(
            @PathVariable Long condoId,
            @PathVariable Long complaintId,
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ComplaintResponseCreateRequest request) {

        ComplaintResponseDto response = complaintService.addResponse(condoId, complaintId, currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Resposta adicionada com sucesso"));
    }

    @PatchMapping("/{complaintId}/status")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long condoId,
            @PathVariable Long complaintId,
            @Valid @RequestBody ComplaintStatusUpdateRequest request) {

        complaintService.updateStatus(condoId, complaintId, request.status());
        return ResponseEntity.ok(ApiResponse.ok(null, "Status atualizado com sucesso"));
    }

    /** Minhas denúncias (morador). Filtro por status opcional. Rota /my e /mine. */
    @GetMapping({ "/my", "/mine" })
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO', 'MORADOR') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ComplaintListResponse>>> getMyComplaints(
            @PathVariable Long condoId,
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {

        ComplaintStatus complaintStatus = null;
        if (status != null && !status.isBlank()) {
            complaintStatus = ComplaintStatus.valueOf(status.toUpperCase());
        }
        Page<ComplaintListResponse> page = complaintService.getMyComplaints(condoId, currentUser.getId(), complaintStatus, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    private boolean hasAnyAuthority(UserPrincipal user, String... authorities) {
        for (String authority : authorities) {
            for (GrantedAuthority ga : user.getAuthorities()) {
                if (ga.getAuthority().equals(authority)) {
                    return true;
                }
            }
        }
        return false;
    }
}
