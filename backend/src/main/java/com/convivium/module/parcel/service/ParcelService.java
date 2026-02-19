package com.convivium.module.parcel.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.common.util.CodeGenerator;
import com.convivium.module.condominium.entity.Unit;
import com.convivium.module.condominium.repository.UnitRepository;
import com.convivium.module.parcel.dto.ParcelCreateRequest;
import com.convivium.module.parcel.dto.ParcelDetailResponse;
import com.convivium.module.parcel.dto.ParcelListResponse;
import com.convivium.module.parcel.dto.ParcelPhotoDto;
import com.convivium.module.parcel.dto.ParcelVerifyRequest;
import com.convivium.module.parcel.entity.Parcel;
import com.convivium.module.parcel.entity.ParcelPhoto;
import com.convivium.module.parcel.entity.ParcelStatus;
import com.convivium.module.parcel.entity.ParcelVerification;
import com.convivium.module.parcel.repository.ParcelRepository;
import com.convivium.module.parcel.repository.ParcelVerificationRepository;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.UserPrincipal;
import com.convivium.integration.whatsapp.WhatsAppNotificationService;
import com.convivium.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final ParcelVerificationRepository parcelVerificationRepository;
    private final UserRepository userRepository;
    private final UnitRepository unitRepository;
    private final FileStorageService fileStorageService;
    private final WhatsAppNotificationService whatsAppService;

    @Transactional(readOnly = true)
    public Page<ParcelListResponse> listParcels(Long condoId, ParcelStatus status, Pageable pageable) {
        Page<Parcel> parcels;
        if (status == null) {
            parcels = parcelRepository.findByCondominiumId(condoId, pageable);
        } else {
            parcels = parcelRepository.findByCondominiumIdAndStatus(condoId, status, pageable);
        }
        return parcels.map(this::toListResponse);
    }

    public ParcelListResponse createParcel(Long condoId, Long receivedById, ParcelCreateRequest request) {
        User receivedBy = userRepository.findById(receivedById)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", receivedById));

        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidade", request.unitId()));

        User recipient = null;
        if (request.recipientId() != null) {
            recipient = userRepository.findById(request.recipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destinatario", request.recipientId()));
        }

        // Um único código: só o morador sabe; na retirada ele informa ao porteiro, que digita no sistema para validar.
        String residentCode = CodeGenerator.generateNumericCode(6);

        Parcel parcel = Parcel.builder()
                .condominiumId(condoId)
                .unit(unit)
                .recipient(recipient)
                .receivedBy(receivedBy)
                .carrier(request.carrier())
                .trackingNumber(request.trackingNumber())
                .description(request.description())
                .status(ParcelStatus.RECEIVED)
                .pickupCode(null)
                .residentCode(residentCode)
                .build();

        parcel = parcelRepository.save(parcel);

        // Notificação WhatsApp (Meta): morador recebe mensagem com código para retirada
        if (recipient != null && recipient.getPhone() != null && !recipient.getPhone().isBlank()) {
            whatsAppService.sendEncomendaNotification(
                    recipient.getPhone(),
                    recipient.getName(),
                    residentCode);
        }

        return toListResponse(parcel);
    }

    public void addPhoto(Long condoId, Long parcelId, MultipartFile file, String photoType, Long uploadedById) {
        Parcel parcel = findParcelOrThrow(condoId, parcelId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo de foto e obrigatorio");
        }
        String type = photoType != null ? photoType.toUpperCase() : "RECEIPT";
        if (!List.of("RECEIPT", "PICKUP", "LABEL").contains(type)) {
            type = "RECEIPT";
        }
        String ext = "jpg";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
            if (!List.of("jpg", "jpeg", "png", "gif", "webp").contains(ext)) {
                ext = "jpg";
            }
        }
        String relativePath = "parcels/" + parcelId + "/" + UUID.randomUUID() + "." + ext;
        String photoUrl = fileStorageService.store(relativePath, file);

        ParcelPhoto photo = ParcelPhoto.builder()
                .parcel(parcel)
                .photoUrl(photoUrl)
                .photoType(type)
                .uploadedBy(uploadedById)
                .build();
        parcel.getPhotos().add(photo);
        parcelRepository.save(parcel);

        // Envia foto da encomenda por WhatsApp (após template, na janela de 24h)
        if (parcel.getPhotos().size() == 1 && parcel.getRecipient() != null) {
            User recipient = parcel.getRecipient();
            if (recipient.getPhone() != null && !recipient.getPhone().isBlank()) {
                whatsAppService.sendEncomendaPhoto(recipient.getPhone(), photoUrl);
            }
        }
    }

    @Transactional(readOnly = true)
    public ParcelDetailResponse getParcel(Long condoId, Long parcelId, UserPrincipal currentUser) {
        Parcel parcel = findParcelOrThrow(condoId, parcelId);

        boolean canManageParcels = hasAuthority(currentUser, "MANAGE_PARCELS")
                || hasAnyRole(currentUser, "SINDICO", "SUB_SINDICO", "PORTEIRO", "PLATFORM_ADMIN");
        boolean isRecipient = parcel.getRecipient() != null
                && parcel.getRecipient().getId().equals(currentUser.getId());

        if (!canManageParcels && !isRecipient) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "So pode ver suas proprias encomendas.");
        }

        // Um único código: só o morador (destinatário) vê. Porteiro nunca vê o código até o morador informar na retirada.
        String pickupCode = null;
        String residentCode = isRecipient ? parcel.getResidentCode() : null;

        List<ParcelPhotoDto> photoDtos = parcel.getPhotos().stream()
                .map(this::toPhotoDto)
                .toList();

        String unitIdentifier = parcel.getUnit() != null ? parcel.getUnit().getIdentifier() : null;
        String recipientName = parcel.getRecipient() != null ? parcel.getRecipient().getName() : null;
        String receivedByName = parcel.getReceivedBy() != null ? parcel.getReceivedBy().getName() : null;

        return new ParcelDetailResponse(
                parcel.getId(),
                unitIdentifier,
                recipientName,
                receivedByName,
                parcel.getCarrier(),
                parcel.getTrackingNumber(),
                parcel.getDescription(),
                parcel.getStatus().name(),
                parcel.getDeliveredAt() != null ? parcel.getDeliveredAt().toString() : null,
                parcel.getCreatedAt() != null ? parcel.getCreatedAt().toString() : null,
                pickupCode,
                residentCode,
                photoDtos
        );
    }

    /** Gera um único código para a encomenda; só o morador vê (em Minhas Encomendas) e informa ao porteiro na retirada. */
    public Map<String, String> generateCodes(Long condoId, Long parcelId) {
        Parcel parcel = findParcelOrThrow(condoId, parcelId);

        String code = CodeGenerator.generateNumericCode(6);
        parcel.setPickupCode(null);
        parcel.setResidentCode(code);
        parcelRepository.save(parcel);

        return Map.of("residentCode", code);
    }

    /** Valida a retirada: o porteiro digita o código que o morador informou (único código; só o morador sabe). */
    public void verifyPickup(Long condoId, Long parcelId, ParcelVerifyRequest request,
                             Long doormanId) {
        Parcel parcel = findParcelOrThrow(condoId, parcelId);

        if (parcel.getResidentCode() == null || !parcel.getResidentCode().equals(request.code())) {
            throw new BusinessException("Codigo informado nao confere. Peça ao morador o codigo que aparece em Minhas Encomendas.", "CODE_MISMATCH");
        }

        User doorman = userRepository.findById(doormanId)
                .orElseThrow(() -> new ResourceNotFoundException("Porteiro", doormanId));

        if (parcel.getRecipient() == null) {
            throw new BusinessException("Encomenda nao possui destinatario cadastrado", "NO_RECIPIENT");
        }
        User resident = parcel.getRecipient();

        ParcelVerification verification = ParcelVerification.builder()
                .parcel(parcel)
                .doorman(doorman)
                .resident(resident)
                .verificationMethod(request.verificationMethod())
                .doormanCode(null)
                .residentCode(request.code())
                .isVerified(true)
                .verifiedAt(Instant.now())
                .build();

        parcelVerificationRepository.save(verification);

        parcel.setStatus(ParcelStatus.DELIVERED);
        parcel.setDeliveredAt(Instant.now());
        parcel.setPickedUpBy(resident);
        parcelRepository.save(parcel);
    }

    @Transactional(readOnly = true)
    public Page<ParcelListResponse> getMyParcels(Long condoId, Long userId, ParcelStatus status, Pageable pageable) {
        Page<Parcel> parcels = status == null
                ? parcelRepository.findByRecipientId(userId, pageable)
                : parcelRepository.findByRecipientIdAndStatus(userId, status, pageable);
        return parcels.map(this::toListResponse);
    }

    // ---- Private helpers ----

    private Parcel findParcelOrThrow(Long condoId, Long parcelId) {
        return parcelRepository.findByIdAndCondominiumId(parcelId, condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Encomenda", parcelId));
    }

    private ParcelListResponse toListResponse(Parcel parcel) {
        String unitIdentifier = parcel.getUnit() != null ? parcel.getUnit().getIdentifier() : null;
        String recipientName = parcel.getRecipient() != null ? parcel.getRecipient().getName() : null;
        String receivedByName = parcel.getReceivedBy() != null ? parcel.getReceivedBy().getName() : null;

        return new ParcelListResponse(
                parcel.getId(),
                unitIdentifier,
                recipientName,
                receivedByName,
                parcel.getCarrier(),
                parcel.getTrackingNumber(),
                parcel.getDescription(),
                parcel.getStatus().name(),
                parcel.getDeliveredAt() != null ? parcel.getDeliveredAt().toString() : null,
                parcel.getCreatedAt() != null ? parcel.getCreatedAt().toString() : null
        );
    }

    private ParcelPhotoDto toPhotoDto(ParcelPhoto photo) {
        return new ParcelPhotoDto(
                photo.getId(),
                photo.getPhotoUrl(),
                photo.getPhotoType(),
                photo.getCreatedAt() != null ? photo.getCreatedAt().toString() : null
        );
    }

    private boolean hasAuthority(UserPrincipal user, String authority) {
        for (GrantedAuthority ga : user.getAuthorities()) {
            if (ga.getAuthority().equals(authority)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyRole(UserPrincipal user, String... roles) {
        for (String role : roles) {
            String roleAuthority = "ROLE_" + role;
            if (hasAuthority(user, roleAuthority)) {
                return true;
            }
        }
        return false;
    }
}
