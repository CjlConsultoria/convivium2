package com.convivium.module.condominium.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.module.condominium.dto.BuildingCreateRequest;
import com.convivium.module.condominium.dto.CondominiumCreateRequest;
import com.convivium.module.condominium.dto.CondominiumResponse;
import com.convivium.module.condominium.dto.UnitCreateRequest;
import com.convivium.module.condominium.entity.Building;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Unit;
import com.convivium.module.condominium.repository.BuildingRepository;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.repository.PlanRepository;
import com.convivium.module.condominium.repository.UnitRepository;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CondominiumServiceTest {

    @Mock
    private CondominiumRepository condominiumRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private BuildingRepository buildingRepository;
    @Mock
    private UnitRepository unitRepository;
    @Mock
    private UserCondominiumRoleRepository userCondominiumRoleRepository;

    @InjectMocks
    private CondominiumService condominiumService;

    @Test
    void listAll_returnsPage() {
        Condominium condo = Condominium.builder().id(1L).name("Condo").slug("condo").status("ACTIVE").build();
        when(condominiumRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(condo)));
        Page<CondominiumResponse> result = condominiumService.listAll(Pageable.unpaged());
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void create_throwsWhenCnpjExists() {
        when(condominiumRepository.existsBySlug(any())).thenReturn(false);
        when(condominiumRepository.existsByCnpj("12.345.678/0001-99")).thenReturn(true);
        CondominiumCreateRequest req = new CondominiumCreateRequest("Condo", "12.345.678/0001-99", null, null, null, null, null, null, null, null, null);
        assertThrows(BusinessException.class, () -> condominiumService.create(req));
    }

    @Test
    void create_success() {
        Condominium saved = Condominium.builder().id(1L).name("Condo").slug("condo").status("ACTIVE").build();
        when(condominiumRepository.existsBySlug(any())).thenReturn(false);
        when(condominiumRepository.save(any())).thenReturn(saved);
        CondominiumCreateRequest req = new CondominiumCreateRequest("Condo", null, null, null, null, null, null, null, null, null, null);
        CondominiumResponse r = condominiumService.create(req);
        assertNotNull(r);
        assertEquals("Condo", r.name());
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(condominiumRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> condominiumService.getById(999L));
    }

    @Test
    void getById_success() {
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).name("Condo").slug("c").status("ACTIVE").build()));
        CondominiumResponse r = condominiumService.getById(1L);
        assertNotNull(r);
        assertEquals("Condo", r.name());
    }

    @Test
    void updateStatus_success() {
        Condominium c = Condominium.builder().id(1L).name("C").slug("c").status("ACTIVE").build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(c));
        when(condominiumRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        condominiumService.updateStatus(1L, "SUSPENDED");
        verify(condominiumRepository).save(argThat(x -> "SUSPENDED".equals(x.getStatus())));
    }

    @Test
    void listBuildings_returnsList() {
        Building b = new Building();
        b.setId(1L);
        b.setName("A");
        b.setFloors(5);
        b.setCondominiumId(1L);
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(buildingRepository.findByCondominiumId(1L)).thenReturn(List.of(b));
        var result = condominiumService.listBuildings(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).name());
    }

    @Test
    void createBuilding_success() {
        Building b = new Building();
        b.setId(1L);
        b.setName("Bloco A");
        b.setFloors(10);
        b.setCondominiumId(1L);
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(buildingRepository.save(any())).thenReturn(b);
        var r = condominiumService.createBuilding(1L, new BuildingCreateRequest("Bloco A", 10));
        assertNotNull(r);
        assertEquals("Bloco A", r.name());
    }

    @Test
    void createUnit_success() {
        Unit u = Unit.builder().id(1L).condominiumId(1L).identifier("101").floor(1).type("APARTMENT").build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(unitRepository.save(any())).thenReturn(u);
        var r = condominiumService.createUnit(1L, new UnitCreateRequest(null, "101", 1, "APARTMENT", null));
        assertNotNull(r);
        assertEquals("101", r.identifier());
    }

    @Test
    void deleteUnit_throwsWhenUnitInUse() {
        Unit u = Unit.builder().id(1L).condominiumId(1L).build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(unitRepository.findByCondominiumIdAndId(1L, 1L)).thenReturn(Optional.of(u));
        when(userCondominiumRoleRepository.existsByUnit_Id(1L)).thenReturn(true);
        assertThrows(BusinessException.class, () -> condominiumService.deleteUnit(1L, 1L));
    }

    @Test
    void deleteUnit_success() {
        Unit u = Unit.builder().id(1L).condominiumId(1L).build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(unitRepository.findByCondominiumIdAndId(1L, 1L)).thenReturn(Optional.of(u));
        when(userCondominiumRoleRepository.existsByUnit_Id(1L)).thenReturn(false);
        condominiumService.deleteUnit(1L, 1L);
        verify(unitRepository).delete(u);
    }
}
