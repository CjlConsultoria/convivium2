package com.convivium.module.condominium.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.module.condominium.dto.ApplyStructureRequest;
import com.convivium.module.condominium.dto.BuildingCreateRequest;
import com.convivium.module.condominium.dto.CondominiumCreateRequest;
import com.convivium.module.condominium.dto.CondominiumResponse;
import com.convivium.module.condominium.dto.GenerateStructureRequest;
import com.convivium.module.condominium.dto.StructurePreviewResponse;
import com.convivium.module.condominium.dto.UnitCreateRequest;
import com.convivium.module.condominium.entity.Building;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Plan;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

    @Test
    void update_success() {
        Condominium c = Condominium.builder().id(1L).name("Old").slug("old").status("ACTIVE").build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(c));
        when(condominiumRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CondominiumCreateRequest req = new CondominiumCreateRequest("New Name", null, "email@test.com", "11999999999", "Rua", "1", null, "Centro", "SP", "SP", "01234");
        CondominiumResponse r = condominiumService.update(1L, req);
        assertNotNull(r);
        assertEquals("New Name", r.name());
    }

    @Test
    void update_throwsWhenNotFound() {
        when(condominiumRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                condominiumService.update(999L, new CondominiumCreateRequest("X", null, null, null, null, null, null, null, null, null, null)));
    }

    @Test
    void setPlan_success() {
        Condominium c = Condominium.builder().id(1L).name("C").slug("c").status("ACTIVE").build();
        Plan plan = Plan.builder().id(1L).name("Basic").slug("basic").priceCents(0).maxUnits(10).maxUsers(10).build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(c));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(condominiumRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CondominiumResponse r = condominiumService.setPlan(1L, 1L);
        assertNotNull(r);
        verify(condominiumRepository).save(argThat(x -> x.getPlan() != null));
    }

    @Test
    void setPlan_withNullPlan() {
        Condominium c = Condominium.builder().id(1L).name("C").slug("c").status("ACTIVE").build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(c));
        when(condominiumRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        condominiumService.setPlan(1L, null);
        verify(condominiumRepository).save(argThat(x -> x.getPlan() == null));
    }

    @Test
    void listUnits_returnsList() {
        Unit u = Unit.builder().id(1L).condominiumId(1L).identifier("101").floor(1).build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(unitRepository.findByCondominiumId(1L)).thenReturn(List.of(u));
        when(userCondominiumRoleRepository.findByCondominiumIdAndUnitNotNullAndStatus(1L, "ACTIVE")).thenReturn(List.of());
        when(userCondominiumRoleRepository.existsByUnit_Id(1L)).thenReturn(false);
        var result = condominiumService.listUnits(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("101", result.get(0).identifier());
    }

    @Test
    void createUnit_withBuilding() {
        Building b = new Building();
        b.setId(10L);
        b.setName("A");
        b.setCondominiumId(1L);
        Unit u = Unit.builder().id(1L).condominiumId(1L).identifier("101").floor(1).building(b).build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(buildingRepository.findById(10L)).thenReturn(Optional.of(b));
        when(unitRepository.save(any())).thenReturn(u);
        var r = condominiumService.createUnit(1L, new UnitCreateRequest(10L, "101", 1, "APARTMENT", BigDecimal.valueOf(50)));
        assertNotNull(r);
        assertEquals("101", r.identifier());
    }

    @Test
    void createUnit_throwsWhenBuildingNotInCondominium() {
        Building b = new Building();
        b.setId(10L);
        b.setCondominiumId(999L);
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(buildingRepository.findById(10L)).thenReturn(Optional.of(b));
        assertThrows(BusinessException.class, () ->
                condominiumService.createUnit(1L, new UnitCreateRequest(10L, "101", 1, "APARTMENT", null)));
    }

    @Test
    void previewStructure_returnsPreview() {
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        GenerateStructureRequest req = new GenerateStructureRequest(2, 2, 2, "101", 1);
        StructurePreviewResponse r = condominiumService.previewStructure(1L, req);
        assertNotNull(r);
        assertEquals(2, r.buildings().size());
        assertEquals("A", r.buildings().get(0).name());
        assertEquals("B", r.buildings().get(1).name());
        assertEquals(8, r.units().size());
    }

    @Test
    void applyStructure_createsBuildingsAndUnits() {
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        Building b = new Building();
        b.setId(1L);
        b.setName("A");
        b.setFloors(2);
        b.setCondominiumId(1L);
        when(buildingRepository.findByCondominiumIdAndName(1L, "A")).thenReturn(Optional.empty());
        when(buildingRepository.save(any())).thenReturn(b);
        when(unitRepository.existsByCondominiumIdAndBuilding_IdAndIdentifier(1L, 1L, "101")).thenReturn(false);
        when(unitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<StructurePreviewResponse.BuildingPreviewItem> buildings = List.of(
                new StructurePreviewResponse.BuildingPreviewItem("A", 2));
        List<StructurePreviewResponse.UnitPreviewItem> units = List.of(
                new StructurePreviewResponse.UnitPreviewItem("A", 1, "101"));

        condominiumService.applyStructure(1L, new ApplyStructureRequest(buildings, units));
        verify(buildingRepository).save(any());
        verify(unitRepository).save(any());
    }

    @Test
    void generateStructure_createsBuildingsAndUnits() {
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(buildingRepository.findByCondominiumIdAndName(1L, "A")).thenReturn(Optional.empty());
        Building b = new Building();
        b.setId(1L);
        b.setName("A");
        b.setFloors(2);
        b.setCondominiumId(1L);
        when(buildingRepository.save(any())).thenReturn(b);
        when(unitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        condominiumService.generateStructure(1L, new GenerateStructureRequest(1, 2, 2, "101", 1));
        verify(buildingRepository).save(any());
        verify(unitRepository, atLeast(1)).save(any());
    }

    @Test
    void create_slugWithTimestampWhenDuplicate() {
        when(condominiumRepository.existsBySlug("condo")).thenReturn(true);
        Condominium saved = Condominium.builder().id(1L).name("Condo").slug("condo-123").status("ACTIVE").build();
        when(condominiumRepository.save(any())).thenReturn(saved);
        CondominiumCreateRequest req = new CondominiumCreateRequest("Condo", null, null, null, null, null, null, null, null, null, null);
        CondominiumResponse r = condominiumService.create(req);
        assertNotNull(r);
        assertTrue(r.slug().contains("condo"));
    }

    @Test
    void deleteBuilding_throwsWhenBuildingNotInCondominium() {
        Building b = new Building();
        b.setId(1L);
        b.setCondominiumId(999L);
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(b));
        assertThrows(BusinessException.class, () -> condominiumService.deleteBuilding(1L, 1L));
    }

    @Test
    void deleteBuilding_throwsWhenUnitsInUse() {
        Building b = new Building();
        b.setId(1L);
        b.setCondominiumId(1L);
        Unit u = Unit.builder().id(10L).condominiumId(1L).building(b).build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(b));
        when(unitRepository.findByCondominiumIdAndBuilding_Id(1L, 1L)).thenReturn(List.of(u));
        when(userCondominiumRoleRepository.existsByUnit_Id(10L)).thenReturn(true);
        assertThrows(BusinessException.class, () -> condominiumService.deleteBuilding(1L, 1L));
    }

    @Test
    void deleteBuilding_success() {
        Building b = new Building();
        b.setId(1L);
        b.setCondominiumId(1L);
        Unit u = Unit.builder().id(10L).condominiumId(1L).building(b).build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).build()));
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(b));
        when(unitRepository.findByCondominiumIdAndBuilding_Id(1L, 1L)).thenReturn(List.of(u));
        when(userCondominiumRoleRepository.existsByUnit_Id(10L)).thenReturn(false);
        condominiumService.deleteBuilding(1L, 1L);
        verify(unitRepository).delete(u);
        verify(buildingRepository).delete(b);
    }
}
