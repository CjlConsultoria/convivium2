package com.convivium.module.dashboard.service;

import com.convivium.common.dto.PageResponse;
import com.convivium.module.complaint.entity.Complaint;
import com.convivium.module.complaint.entity.ComplaintStatus;
import com.convivium.module.complaint.repository.ComplaintRepository;
import com.convivium.module.dashboard.dto.DashboardStatsResponse;
import com.convivium.module.dashboard.dto.UnitActivityItemDto;
import com.convivium.module.parcel.entity.Parcel;
import com.convivium.module.parcel.entity.ParcelStatus;
import com.convivium.module.parcel.repository.ParcelRepository;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import com.convivium.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserCondominiumRoleRepository userCondominiumRoleRepository;
    private final ComplaintRepository complaintRepository;
    private final ParcelRepository parcelRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(Long condominiumId, UserPrincipal currentUser) {
        boolean isMorador = currentUser.getAuthorities().stream()
                .anyMatch(a -> "ROLE_MORADOR".equals(a.getAuthority()));

        if (isMorador) {
            return getStatsForMorador(condominiumId, currentUser.getId());
        }

        long totalMoradores = userCondominiumRoleRepository.countByCondominiumId(condominiumId);
        long denunciasAbertas = complaintRepository.countByCondominiumIdAndStatus(condominiumId, ComplaintStatus.OPEN);
        long encomendasPendentes = parcelRepository.countByCondominiumIdAndStatusNot(condominiumId, ParcelStatus.DELIVERED);
        return DashboardStatsResponse.builder()
                .totalMoradores(totalMoradores)
                .denunciasAbertas(denunciasAbertas)
                .encomendasPendentes(encomendasPendentes)
                .reservasHoje(0L)
                .build();
    }

    private DashboardStatsResponse getStatsForMorador(Long condominiumId, Long userId) {
        // Morador: denúncias abertas = minhas denúncias (que eu criei); encomendas = minhas (onde sou destinatário)
        long denunciasAbertas = complaintRepository.countByCondominiumIdAndComplainantIdAndStatus(
                condominiumId, userId, ComplaintStatus.OPEN);
        long encomendasPendentes = parcelRepository.countByRecipientIdAndStatusNot(userId, ParcelStatus.DELIVERED);

        return DashboardStatsResponse.builder()
                .totalMoradores(0L)
                .denunciasAbertas(denunciasAbertas)
                .encomendasPendentes(encomendasPendentes)
                .reservasHoje(0L)
                .build();
    }

    /**
     * Últimos 30 dias de atividade: morador vê só as suas denúncias e as suas encomendas;
     * funcionário com unidade vê da unidade; sem unidade lista vazia.
     */
    @Transactional(readOnly = true)
    public PageResponse<UnitActivityItemDto> getUnitActivity(Long condominiumId, UserPrincipal currentUser, int page, int size) {
        boolean isMorador = currentUser.getAuthorities().stream()
                .anyMatch(a -> "ROLE_MORADOR".equals(a.getAuthority()));
        Long userId = currentUser.getId();
        Instant since = Instant.now().minusSeconds(30L * 24 * 60 * 60);

        List<Complaint> complaints;
        List<Parcel> parcels;

        if (isMorador) {
            // Morador: minhas denúncias (que eu abri) e minhas encomendas (onde eu sou destinatário)
            complaints = complaintRepository
                    .findByCondominiumIdAndComplainantIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(condominiumId, userId, since);
            parcels = parcelRepository
                    .findByCondominiumIdAndRecipientIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(condominiumId, userId, since);
        } else {
            Long unitId = userCondominiumRoleRepository.findByUserIdAndCondominiumId(userId, condominiumId)
                    .filter(ucr -> ucr.getUnit() != null)
                    .map(ucr -> ucr.getUnit().getId())
                    .orElse(null);
            if (unitId == null) {
                return emptyActivityPage(page, size);
            }
            complaints = complaintRepository
                    .findByCondominiumIdAndUnit_IdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(condominiumId, unitId, since);
            parcels = parcelRepository
                    .findByCondominiumIdAndUnit_IdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(condominiumId, unitId, since);
        }

        List<UnitActivityItemDto> all = new ArrayList<>();
        for (Complaint c : complaints) {
            String descComplaint = c.getDescription() != null ? c.getDescription() : "";
            if (descComplaint.length() > 80) descComplaint = descComplaint.substring(0, 80) + "...";
            all.add(new UnitActivityItemDto("COMPLAINT", c.getId(), c.getTitle(),
                    descComplaint,
                    c.getCreatedAt(), "Denúncia"));
        }
        for (Parcel p : parcels) {
            String desc = p.getDescription() != null ? p.getDescription() : (p.getCarrier() != null ? p.getCarrier() : "Encomenda");
            if (desc.length() > 80) desc = desc.substring(0, 80) + "...";
            all.add(new UnitActivityItemDto("PARCEL", p.getId(), "Encomenda",
                    desc,
                    p.getCreatedAt(), "Encomenda"));
        }
        all.sort(Comparator.comparing(UnitActivityItemDto::date).reversed());

        int total = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<UnitActivityItemDto> content = from < total ? all.subList(from, to) : List.of();

        PageResponse<UnitActivityItemDto> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(total);
        response.setTotalPages(totalPages);
        response.setLast(page >= totalPages - 1);
        return response;
    }

    private PageResponse<UnitActivityItemDto> emptyActivityPage(int page, int size) {
        PageResponse<UnitActivityItemDto> response = new PageResponse<>();
        response.setContent(List.of());
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(0);
        response.setTotalPages(0);
        response.setLast(true);
        return response;
    }
}
