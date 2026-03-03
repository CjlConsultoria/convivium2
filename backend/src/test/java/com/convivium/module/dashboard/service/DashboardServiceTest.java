package com.convivium.module.dashboard.service;

import com.convivium.module.complaint.entity.ComplaintStatus;
import com.convivium.module.complaint.repository.ComplaintRepository;
import com.convivium.module.parcel.entity.ParcelStatus;
import com.convivium.module.parcel.repository.ParcelRepository;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import com.convivium.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
}
