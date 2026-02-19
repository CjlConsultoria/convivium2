package com.convivium.module.user.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.common.util.CpfUtil;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Unit;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.repository.UnitRepository;
import com.convivium.module.user.dto.UserCreateRequest;
import com.convivium.module.user.dto.UserResponse;
import com.convivium.module.user.dto.UserUpdateRequest;
import com.convivium.module.user.entity.Role;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.entity.UserCondominiumRole;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import com.convivium.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserCondominiumRoleRepository userCondominiumRoleRepository;
    private final CondominiumRepository condominiumRepository;
    private final UnitRepository unitRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Long condominiumId, Pageable pageable) {
        Page<UserCondominiumRole> page = userCondominiumRoleRepository.findByCondominiumId(condominiumId, pageable);
        return page.map(ucr -> mapToResponse(ucr.getUser(), ucr));
    }

    public UserResponse createUser(Long condominiumId, UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email ja cadastrado", "EMAIL_ALREADY_EXISTS");
        }
        String cpfNormalized = CpfUtil.normalize(request.cpf());
        if (cpfNormalized != null && userRepository.existsByCpf(cpfNormalized)) {
            throw new BusinessException("CPF ja cadastrado", "CPF_ALREADY_EXISTS");
        }

        Condominium condominium = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condominiumId));

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .cpf(cpfNormalized)
                .phone(request.phone())
                .isActive(true)
                .build();

        user = userRepository.save(user);

        Role role = Role.valueOf(request.role());

        Unit unit = null;
        if (request.unitId() != null) {
            unit = unitRepository.findByCondominiumIdAndId(condominiumId, request.unitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unidade", request.unitId()));
        }

        UserCondominiumRole ucr = UserCondominiumRole.builder()
                .user(user)
                .condominium(condominium)
                .role(role)
                .unit(unit)
                .status("ACTIVE")
                .build();

        ucr = userCondominiumRoleRepository.save(ucr);

        return mapToResponse(user, ucr);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long condominiumId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        UserCondominiumRole ucr = userCondominiumRoleRepository
                .findByUserIdAndCondominiumId(userId, condominiumId)
                .orElseThrow(() -> new BusinessException(
                        "Usuario nao pertence a este condominio", "USER_NOT_IN_CONDOMINIUM"));

        return mapToResponse(user, ucr);
    }

    public UserResponse updateUser(Long condominiumId, Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        UserCondominiumRole ucr = userCondominiumRoleRepository
                .findByUserIdAndCondominiumId(userId, condominiumId)
                .orElseThrow(() -> new BusinessException(
                        "Usuario nao pertence a este condominio", "USER_NOT_IN_CONDOMINIUM"));

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.photoUrl() != null) {
            user.setPhotoUrl(request.photoUrl());
        }
        if (request.isActive() != null) {
            user.setActive(request.isActive());
        }

        user = userRepository.save(user);

        if (request.unitId() != null) {
            Unit unit = unitRepository.findByCondominiumIdAndId(condominiumId, request.unitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unidade", request.unitId()));
            ucr.setUnit(unit);
            ucr = userCondominiumRoleRepository.save(ucr);
        }

        return mapToResponse(user, ucr);
    }

    public void deleteUser(Long condominiumId, Long userId) {
        UserCondominiumRole ucr = userCondominiumRoleRepository
                .findByUserIdAndCondominiumId(userId, condominiumId)
                .orElseThrow(() -> new BusinessException(
                        "Usuario nao pertence a este condominio", "USER_NOT_IN_CONDOMINIUM"));

        userCondominiumRoleRepository.delete(ucr);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingApprovals(Long condominiumId) {
        List<UserCondominiumRole> pendingRoles = userCondominiumRoleRepository
                .findByCondominiumIdAndStatus(condominiumId, "PENDING_APPROVAL");

        return pendingRoles.stream()
                .map(ucr -> mapToResponse(ucr.getUser(), ucr))
                .toList();
    }

    public void approveUser(Long condominiumId, Long userId, Long approvedById, Long unitIdOptional) {
        UserCondominiumRole ucr = userCondominiumRoleRepository
                .findByUserIdAndCondominiumId(userId, condominiumId)
                .orElseThrow(() -> new BusinessException(
                        "Usuario nao pertence a este condominio", "USER_NOT_IN_CONDOMINIUM"));

        if (!"PENDING_APPROVAL".equals(ucr.getStatus())) {
            throw new BusinessException("Usuario nao esta pendente de aprovacao", "USER_NOT_PENDING");
        }

        if (unitIdOptional != null) {
            Unit unit = unitRepository.findByCondominiumIdAndId(condominiumId, unitIdOptional)
                    .orElseThrow(() -> new ResourceNotFoundException("Unidade", unitIdOptional));
            ucr.setUnit(unit);
        }

        ucr.setStatus("ACTIVE");
        ucr.setApprovedBy(approvedById);
        ucr.setApprovedAt(Instant.now());

        userCondominiumRoleRepository.save(ucr);
    }

    public void rejectUser(Long condominiumId, Long userId) {
        UserCondominiumRole ucr = userCondominiumRoleRepository
                .findByUserIdAndCondominiumId(userId, condominiumId)
                .orElseThrow(() -> new BusinessException(
                        "Usuario nao pertence a este condominio", "USER_NOT_IN_CONDOMINIUM"));

        if (!"PENDING_APPROVAL".equals(ucr.getStatus())) {
            throw new BusinessException("Usuario nao esta pendente de aprovacao", "USER_NOT_PENDING");
        }

        userCondominiumRoleRepository.delete(ucr);
    }

    private UserResponse mapToResponse(User user, UserCondominiumRole ucr) {
        String unitIdentifier = null;
        Long unitId = null;

        if (ucr.getUnit() != null) {
            unitId = ucr.getUnit().getId();
            unitIdentifier = ucr.getUnit().getIdentifier();
        }

        return new UserResponse(
                user.getId(),
                user.getUuid() != null ? user.getUuid().toString() : null,
                user.getEmail(),
                user.getName(),
                user.getCpf(),
                user.getPhone(),
                user.getPhotoUrl(),
                user.isActive(),
                ucr.getRole().name(),
                ucr.getStatus(),
                unitId,
                unitIdentifier,
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }
}
