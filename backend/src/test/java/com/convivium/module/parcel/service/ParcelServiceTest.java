package com.convivium.module.parcel.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.module.condominium.entity.Unit;
import com.convivium.module.condominium.repository.UnitRepository;
import com.convivium.module.parcel.dto.ParcelCreateRequest;
import com.convivium.module.parcel.dto.ParcelListResponse;
import com.convivium.module.parcel.dto.ParcelVerifyRequest;
import com.convivium.module.parcel.entity.Parcel;
import com.convivium.module.parcel.entity.ParcelStatus;
import com.convivium.module.parcel.repository.ParcelRepository;
import com.convivium.module.parcel.repository.ParcelVerificationRepository;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.UserPrincipal;
import com.convivium.integration.whatsapp.WhatsAppNotificationService;
import com.convivium.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelServiceTest {

    @Mock
    private ParcelRepository parcelRepository;
    @Mock
    private ParcelVerificationRepository parcelVerificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UnitRepository unitRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private WhatsAppNotificationService whatsAppService;

    @InjectMocks
    private ParcelService parcelService;

    @Test
    void listParcels_returnsPage() {
        Parcel p = createParcel(1L);
        when(parcelRepository.findByCondominiumId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));
        Page<ParcelListResponse> result = parcelService.listParcels(1L, null, Pageable.unpaged());
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listParcels_withStatus() {
        Parcel p = createParcel(1L);
        when(parcelRepository.findByCondominiumIdAndStatus(eq(1L), eq(ParcelStatus.RECEIVED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));
        Page<ParcelListResponse> result = parcelService.listParcels(1L, ParcelStatus.RECEIVED, Pageable.unpaged());
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void createParcel_throwsWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ParcelCreateRequest req = new ParcelCreateRequest(10L, null, null, null, null);
        assertThrows(ResourceNotFoundException.class, () -> parcelService.createParcel(1L, 1L, req));
    }

    @Test
    void createParcel_throwsWhenUnitNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L)));
        when(unitRepository.findById(10L)).thenReturn(Optional.empty());
        ParcelCreateRequest req = new ParcelCreateRequest(10L, null, null, null, null);
        assertThrows(ResourceNotFoundException.class, () -> parcelService.createParcel(1L, 1L, req));
    }

    @Test
    void createParcel_success() {
        User receivedBy = createUser(1L);
        Unit unit = Unit.builder().id(10L).identifier("101").condominiumId(1L).build();
        Parcel saved = createParcel(1L);
        saved.setUnit(unit);
        saved.setReceivedBy(receivedBy);

        when(userRepository.findById(1L)).thenReturn(Optional.of(receivedBy));
        when(unitRepository.findById(10L)).thenReturn(Optional.of(unit));
        when(parcelRepository.save(any())).thenReturn(saved);

        ParcelCreateRequest req = new ParcelCreateRequest(10L, null, "Correios", null, "Pacote");
        ParcelListResponse r = parcelService.createParcel(1L, 1L, req);

        assertNotNull(r);
        verify(parcelRepository).save(any(Parcel.class));
    }

    @Test
    void getParcel_throwsWhenNotFound() {
        when(parcelRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.empty());
        UserPrincipal principal = createPrincipal(1L, "ROLE_SINDICO");
        assertThrows(ResourceNotFoundException.class, () -> parcelService.getParcel(1L, 1L, principal));
    }

    @Test
    void getParcel_successForSindico() {
        Parcel p = createParcel(1L);
        p.setRecipient(createUser(2L));
        when(parcelRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.of(p));
        UserPrincipal principal = createPrincipal(1L, "ROLE_SINDICO");
        var r = parcelService.getParcel(1L, 1L, principal);
        assertNotNull(r);
    }

    @Test
    void verifyPickup_throwsWhenCodeMismatch() {
        Parcel p = createParcel(1L);
        p.setResidentCode("123456");
        p.setRecipient(createUser(2L));
        when(parcelRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.of(p));
        ParcelVerifyRequest req = new ParcelVerifyRequest("999999", "CODE");
        assertThrows(BusinessException.class, () -> parcelService.verifyPickup(1L, 1L, req, 1L));
    }

    @Test
    void getMyParcels_returnsPage() {
        Parcel p = createParcel(1L);
        when(parcelRepository.findByRecipientId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));
        Page<ParcelListResponse> result = parcelService.getMyParcels(1L, 1L, null, Pageable.unpaged());
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getMyParcels_withStatus() {
        Parcel p = createParcel(1L);
        when(parcelRepository.findByRecipientIdAndStatus(eq(1L), eq(ParcelStatus.RECEIVED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));
        Page<ParcelListResponse> result = parcelService.getMyParcels(1L, 1L, ParcelStatus.RECEIVED, Pageable.unpaged());
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void generateCodes_returnsNewCode() {
        Parcel p = createParcel(1L);
        p.setResidentCode("old");
        when(parcelRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.of(p));
        when(parcelRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        var result = parcelService.generateCodes(1L, 1L);
        assertNotNull(result);
        assertTrue(result.containsKey("residentCode"));
        assertEquals(6, result.get("residentCode").length());
    }

    @Test
    void verifyPickup_success() {
        Parcel p = createParcel(1L);
        p.setResidentCode("123456");
        p.setRecipient(createUser(2L));
        User doorman = createUser(1L);
        when(parcelRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.of(p));
        when(userRepository.findById(1L)).thenReturn(Optional.of(doorman));
        when(parcelVerificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(parcelRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        parcelService.verifyPickup(1L, 1L, new ParcelVerifyRequest("123456", "CODE"), 1L);
        verify(parcelRepository).save(argThat(x -> ParcelStatus.DELIVERED == x.getStatus()));
    }

    @Test
    void verifyPickup_throwsWhenNoRecipient() {
        Parcel p = createParcel(1L);
        p.setResidentCode("123456");
        p.setRecipient(null);
        when(parcelRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.of(p));
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L)));
        assertThrows(BusinessException.class, () ->
                parcelService.verifyPickup(1L, 1L, new ParcelVerifyRequest("123456", "CODE"), 1L));
    }

    @Test
    void getParcel_successForRecipient() {
        Parcel p = createParcel(1L);
        User recipient = createUser(2L);
        p.setRecipient(recipient);
        p.setResidentCode("123456");
        when(parcelRepository.findByIdAndCondominiumId(1L, 1L)).thenReturn(Optional.of(p));
        UserPrincipal principal = createPrincipal(2L, "ROLE_MORADOR");
        var r = parcelService.getParcel(1L, 1L, principal);
        assertNotNull(r);
        assertNotNull(r.residentCode());
    }

    @Test
    void createParcel_withRecipient() {
        User receivedBy = createUser(1L);
        User recipient = createUser(2L);
        recipient.setPhone("11999999999");
        Unit unit = Unit.builder().id(10L).identifier("101").condominiumId(1L).build();
        Parcel saved = createParcel(1L);
        saved.setUnit(unit);
        saved.setReceivedBy(receivedBy);
        saved.setRecipient(recipient);
        when(userRepository.findById(1L)).thenReturn(Optional.of(receivedBy));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(unitRepository.findById(10L)).thenReturn(Optional.of(unit));
        when(parcelRepository.save(any())).thenReturn(saved);
        ParcelCreateRequest req = new ParcelCreateRequest(10L, 2L, "Correios", null, "Pacote");
        ParcelListResponse r = parcelService.createParcel(1L, 1L, req);
        assertNotNull(r);
        verify(whatsAppService).sendEncomendaNotification(eq("11999999999"), any(), any());
    }

    private Parcel createParcel(Long id) {
        Parcel p = new Parcel();
        p.setId(id);
        p.setCondominiumId(1L);
        p.setStatus(ParcelStatus.RECEIVED);
        p.setCarrier("Correios");
        p.setUnit(Unit.builder().id(10L).identifier("101").build());
        p.setReceivedBy(createUser(1L));
        return p;
    }

    private User createUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("User " + id);
        u.setEmail("user" + id + "@test.com");
        u.setPhone(null);
        return u;
    }

    private UserPrincipal createPrincipal(Long id, String role) {
        return UserPrincipal.builder()
                .id(id)
                .uuid("uuid")
                .email("a@b.com")
                .password("")
                .name("User")
                .condominiumId(1L)
                .isPlatformAdmin(false)
                .active(true)
                .authorities(new java.util.ArrayList<>(List.of(new SimpleGrantedAuthority(role))))
                .build();
    }
}
