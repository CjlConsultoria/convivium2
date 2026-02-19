package com.convivium.module.complaint.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.module.complaint.dto.ComplaintAttachmentDto;
import com.convivium.module.complaint.dto.ComplaintCreateRequest;
import com.convivium.module.complaint.dto.ComplaintDetailResponse;
import com.convivium.module.complaint.dto.ComplaintListResponse;
import com.convivium.module.complaint.dto.ComplaintResponseCreateRequest;
import com.convivium.module.complaint.dto.ComplaintResponseDto;
import com.convivium.module.complaint.entity.Complaint;
import com.convivium.module.complaint.entity.ComplaintAttachment;
import com.convivium.module.complaint.entity.ComplaintCategory;
import com.convivium.module.complaint.entity.ComplaintResponse;
import com.convivium.module.complaint.entity.ComplaintStatus;
import com.convivium.module.complaint.repository.ComplaintRepository;
import com.convivium.module.complaint.repository.ComplaintResponseRepository;
import com.convivium.module.condominium.entity.Unit;
import com.convivium.module.condominium.repository.UnitRepository;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintResponseRepository complaintResponseRepository;
    private final UserRepository userRepository;
    private final UnitRepository unitRepository;

    private static final Set<String> VALID_STATUS_TRANSITIONS = Set.of(
            "OPEN->IN_REVIEW",
            "OPEN->CLOSED",
            "IN_REVIEW->RESPONDED",
            "IN_REVIEW->RESOLVED",
            "IN_REVIEW->CLOSED",
            "RESPONDED->IN_REVIEW",
            "RESPONDED->RESOLVED",
            "RESPONDED->CLOSED",
            "RESOLVED->CLOSED"
    );

    @Transactional(readOnly = true)
    public Page<ComplaintListResponse> listComplaints(Long condoId, ComplaintStatus status, Pageable pageable) {
        Page<Complaint> complaints;
        if (status == null) {
            complaints = complaintRepository.findByCondominiumId(condoId, pageable);
        } else {
            complaints = complaintRepository.findByCondominiumIdAndStatus(condoId, status, pageable);
        }
        return complaints.map(this::toListResponse);
    }

    @Transactional(readOnly = true)
    public ComplaintDetailResponse getComplaint(Long condoId, Long complaintId) {
        Complaint complaint = findComplaintOrThrow(condoId, complaintId);

        List<ComplaintResponse> responses = complaintResponseRepository
                .findByComplaintIdOrderByCreatedAtAsc(complaint.getId());

        List<ComplaintResponseDto> responseDtos = responses.stream()
                .map(this::toResponseDto)
                .toList();

        List<ComplaintAttachmentDto> attachmentDtos = complaint.getAttachments().stream()
                .map(this::toAttachmentDto)
                .toList();

        String complainantName = resolveComplainantName(complaint);
        String unitIdentifier = complaint.getUnit() != null ? complaint.getUnit().getIdentifier() : null;

        return new ComplaintDetailResponse(
                complaint.getId(),
                complainantName,
                complaint.isAnonymous(),
                complaint.getCategory().name(),
                complaint.getTitle(),
                complaint.getDescription(),
                complaint.getStatus().name(),
                complaint.getPriority(),
                unitIdentifier,
                complaint.getCreatedAt() != null ? complaint.getCreatedAt().toString() : null,
                complaint.getUpdatedAt() != null ? complaint.getUpdatedAt().toString() : null,
                responseDtos,
                attachmentDtos
        );
    }

    @Transactional(readOnly = true)
    public ComplaintDetailResponse getComplaintFiltered(Long condoId, Long complaintId, boolean canViewInternal,
                                                        Long currentUserId, boolean isMoradorOnly) {
        Complaint complaint = findComplaintOrThrow(condoId, complaintId);

        if (isMoradorOnly && (complaint.getComplainant() == null
                || !complaint.getComplainant().getId().equals(currentUserId))) {
            throw new AccessDeniedException("So pode ver suas proprias denuncias.");
        }

        List<ComplaintResponse> responses = complaintResponseRepository
                .findByComplaintIdOrderByCreatedAtAsc(complaint.getId());

        List<ComplaintResponseDto> responseDtos = responses.stream()
                .filter(r -> canViewInternal || !r.isInternal())
                .map(this::toResponseDto)
                .toList();

        List<ComplaintAttachmentDto> attachmentDtos = complaint.getAttachments().stream()
                .map(this::toAttachmentDto)
                .toList();

        String complainantName = resolveComplainantName(complaint);
        String unitIdentifier = complaint.getUnit() != null ? complaint.getUnit().getIdentifier() : null;

        return new ComplaintDetailResponse(
                complaint.getId(),
                complainantName,
                complaint.isAnonymous(),
                complaint.getCategory().name(),
                complaint.getTitle(),
                complaint.getDescription(),
                complaint.getStatus().name(),
                complaint.getPriority(),
                unitIdentifier,
                complaint.getCreatedAt() != null ? complaint.getCreatedAt().toString() : null,
                complaint.getUpdatedAt() != null ? complaint.getUpdatedAt().toString() : null,
                responseDtos,
                attachmentDtos
        );
    }

    public ComplaintListResponse createComplaint(Long condoId, Long userId, ComplaintCreateRequest request) {
        User complainant = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        ComplaintCategory category;
        try {
            category = ComplaintCategory.valueOf(request.category().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Categoria invalida: " + request.category(), "INVALID_CATEGORY");
        }

        Unit unit = null;
        if (request.unitId() != null) {
            unit = unitRepository.findById(request.unitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unidade", request.unitId()));
        }

        Complaint complaint = Complaint.builder()
                .condominiumId(condoId)
                .complainant(complainant)
                .isAnonymous(request.isAnonymous())
                .category(category)
                .title(request.title())
                .description(request.description())
                .unit(unit)
                .status(ComplaintStatus.OPEN)
                .priority(request.priority() != null ? request.priority() : "MEDIUM")
                .createdBy(userId)
                .build();

        complaint = complaintRepository.save(complaint);
        return toListResponse(complaint);
    }

    public ComplaintResponseDto addResponse(Long condoId, Long complaintId, Long responderId,
                                            ComplaintResponseCreateRequest request) {
        Complaint complaint = findComplaintOrThrow(condoId, complaintId);

        User responder = userRepository.findById(responderId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", responderId));

        ComplaintResponse response = ComplaintResponse.builder()
                .complaint(complaint)
                .responder(responder)
                .message(request.message())
                .isInternal(request.isInternal())
                .build();

        response = complaintResponseRepository.save(response);

        if (complaint.getStatus() == ComplaintStatus.OPEN) {
            complaint.setStatus(ComplaintStatus.IN_REVIEW);
        }
        if (!request.isInternal() && complaint.getStatus() == ComplaintStatus.IN_REVIEW) {
            complaint.setStatus(ComplaintStatus.RESPONDED);
        }

        complaintRepository.save(complaint);
        return toResponseDto(response);
    }

    public void updateStatus(Long condoId, Long complaintId, String newStatus) {
        Complaint complaint = findComplaintOrThrow(condoId, complaintId);

        ComplaintStatus targetStatus;
        try {
            targetStatus = ComplaintStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status invalido: " + newStatus, "INVALID_STATUS");
        }

        String transition = complaint.getStatus().name() + "->" + targetStatus.name();
        if (!VALID_STATUS_TRANSITIONS.contains(transition)) {
            throw new BusinessException(
                    "Transicao de status invalida: " + complaint.getStatus().name() + " -> " + targetStatus.name(),
                    "INVALID_STATUS_TRANSITION"
            );
        }

        complaint.setStatus(targetStatus);

        if (targetStatus == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(Instant.now());
        }
        if (targetStatus == ComplaintStatus.CLOSED) {
            complaint.setClosedAt(Instant.now());
        }

        complaintRepository.save(complaint);
    }

    @Transactional(readOnly = true)
    public Page<ComplaintListResponse> getMyComplaints(Long condoId, Long userId, ComplaintStatus status, Pageable pageable) {
        Page<Complaint> complaints = status == null
                ? complaintRepository.findByCondominiumIdAndComplainantId(condoId, userId, pageable)
                : complaintRepository.findByCondominiumIdAndComplainantIdAndStatus(condoId, userId, status, pageable);
        return complaints.map(this::toListResponse);
    }

    // ---- Private helpers ----

    private Complaint findComplaintOrThrow(Long condoId, Long complaintId) {
        return complaintRepository.findByIdAndCondominiumId(complaintId, condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Reclamacao", complaintId));
    }

    private String resolveComplainantName(Complaint complaint) {
        if (complaint.isAnonymous()) {
            return null;
        }
        return complaint.getComplainant() != null ? complaint.getComplainant().getName() : null;
    }

    private ComplaintListResponse toListResponse(Complaint complaint) {
        String complainantName = resolveComplainantName(complaint);
        String unitIdentifier = complaint.getUnit() != null ? complaint.getUnit().getIdentifier() : null;
        int responseCount = complaint.getResponses() != null ? complaint.getResponses().size() : 0;

        return new ComplaintListResponse(
                complaint.getId(),
                complainantName,
                complaint.isAnonymous(),
                complaint.getCategory().name(),
                complaint.getTitle(),
                complaint.getStatus().name(),
                complaint.getPriority(),
                unitIdentifier,
                complaint.getCreatedAt() != null ? complaint.getCreatedAt().toString() : null,
                responseCount
        );
    }

    private ComplaintResponseDto toResponseDto(ComplaintResponse response) {
        String responderName = response.getResponder() != null ? response.getResponder().getName() : null;
        String responderRole = null;
        if (response.getResponder() != null && response.getResponder().getCondominiumRoles() != null
                && !response.getResponder().getCondominiumRoles().isEmpty()) {
            responderRole = response.getResponder().getCondominiumRoles().get(0).getRole().name();
        }

        return new ComplaintResponseDto(
                response.getId(),
                responderName,
                responderRole,
                response.getMessage(),
                response.isInternal(),
                response.getCreatedAt() != null ? response.getCreatedAt().toString() : null
        );
    }

    private ComplaintAttachmentDto toAttachmentDto(ComplaintAttachment attachment) {
        return new ComplaintAttachmentDto(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFileUrl(),
                attachment.getFileType(),
                attachment.getCreatedAt() != null ? attachment.getCreatedAt().toString() : null
        );
    }
}
