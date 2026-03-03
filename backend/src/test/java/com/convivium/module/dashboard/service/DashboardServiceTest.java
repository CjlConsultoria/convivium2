package com.convivium.module.dashboard.service;

import com.convivium.module.complaint.entity.Complaint;
import com.convivium.module.complaint.entity.ComplaintStatus;
import com.convivium.module.complaint.repository.ComplaintRepository;
import com.convivium.module.dashboard.dto.UnitActivityItemDto;
import com.convivium.module.parcel.entity.Parcel;
import com.convivium.module.parcel.entity.ParcelStatus;
import com.convivium.module.parcel.repository.ParcelRepository;
import com.convivium.module.condominium.entity.Unit;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import com.convivium.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserCondominiumRoleRepository userCondominiumRoleRepository;

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ParcelRepository parcelRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private UserPrincipal createUser(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        return UserPrincipal.builder()
                .id(1L)
                .uuid("uuid")
                .email("a@b.com")
                .password("")
                .name("User")
                .condominiumId(1L)
                .isPlatformAdmin(false)
                .active(true)
                .authorities(authorities)
                .build();
    }

    @Test
    void getStats_paraSindico_retornaTotais() {
        UserPrincipal user = createUser(List.of("SINDICO"));
        when(userCondominiumRoleRepository.countByCondominiumId(1L)).thenReturn(10L);
        when(complaintRepository.countByCondominiumIdAndStatus(1L, ComplaintStatus.OPEN)).thenReturn(2L);
        when(parcelRepository.countByCondominiumIdAndStatusNot(1L, ParcelStatus.DELIVERED)).thenReturn(1L);

        var stats = dashboardService.getStats(1L, user);

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalMoradores());
    }

    @Test
    void getStats_paraMorador_retornaMinhasEstatisticas() {
        UserPrincipal user = createUser(List.of("MORADOR"));
        when(complaintRepository.countByCondominiumIdAndComplainantIdAndStatus(1L, 1L, ComplaintStatus.OPEN)).thenReturn(0L);
        when(parcelRepository.countByRecipientIdAndStatusNot(1L, ParcelStatus.DELIVERED)).thenReturn(1L);

        var stats = dashboardService.getStats(1L, user);

        assertNotNull(stats);
        assertEquals(1L, stats.getEncomendasPendentes());
    }

    @Test
    void getUnitActivity_paraMorador_retornaDenunciasEEncomendas() {
        UserPrincipal user = createUser(List.of("MORADOR"));
        Complaint c = new Complaint();
        c.setId(1L);
        c.setTitle("Reclamacao");
        c.setDescription("Desc");
        c.setCreatedAt(Instant.now());
        Parcel p = new Parcel();
        p.setId(1L);
        p.setDescription("Pacote");
        p.setCreatedAt(Instant.now());
        when(complaintRepository.findByCondominiumIdAndComplainantIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                eq(1L), eq(1L), any(Instant.class))).thenReturn(List.of(c));
        when(parcelRepository.findByCondominiumIdAndRecipientIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                eq(1L), eq(1L), any(Instant.class))).thenReturn(List.of(p));

        var result = dashboardService.getUnitActivity(1L, user, 0, 10);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().size() >= 1);
    }

    @Test
    void getUnitActivity_paraFuncionarioSemUnidade_retornaVazio() {
        UserPrincipal user = createUser(List.of("SINDICO"));
        when(userCondominiumRoleRepository.findByUserIdAndCondominiumId(1L, 1L)).thenReturn(Optional.empty());

        var result = dashboardService.getUnitActivity(1L, user, 0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
}
