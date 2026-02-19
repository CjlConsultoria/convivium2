package com.convivium.module.user.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.common.dto.PageResponse;
import com.convivium.module.user.dto.ApproveUserRequest;
import com.convivium.module.user.dto.UserCreateRequest;
import com.convivium.module.user.dto.UserResponse;
import com.convivium.module.user.dto.UserUpdateRequest;
import com.convivium.module.user.service.UserService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condos/{condoId}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsers(
            @PathVariable Long condoId,
            @PageableDefault(size = 20) Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {

        Page<UserResponse> page = userService.listUsers(condoId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @PathVariable Long condoId,
            @Valid @RequestBody UserCreateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        UserResponse response = userService.createUser(condoId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Usuario criado com sucesso"));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable Long condoId,
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {

        UserResponse response = userService.getUser(condoId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long condoId,
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        UserResponse response = userService.updateUser(condoId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Usuario atualizado com sucesso"));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SINDICO')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long condoId,
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {

        userService.deleteUser(condoId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Usuario removido do condominio com sucesso"));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getPendingApprovals(
            @PathVariable Long condoId,
            @CurrentUser UserPrincipal currentUser) {

        List<UserResponse> pending = userService.getPendingApprovals(condoId);
        return ResponseEntity.ok(ApiResponse.ok(pending));
    }

    @PatchMapping("/{userId}/approve")
    @PreAuthorize("hasRole('SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveUser(
            @PathVariable Long condoId,
            @PathVariable Long userId,
            @RequestBody(required = false) ApproveUserRequest body,
            @CurrentUser UserPrincipal currentUser) {

        userService.approveUser(condoId, userId, currentUser.getId(), body != null ? body.unitId() : null);
        return ResponseEntity.ok(ApiResponse.ok(null, "Usuario aprovado com sucesso"));
    }

    @PatchMapping("/{userId}/reject")
    @PreAuthorize("hasRole('SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectUser(
            @PathVariable Long condoId,
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {

        userService.rejectUser(condoId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Usuario rejeitado com sucesso"));
    }
}
