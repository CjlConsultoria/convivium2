package com.convivium.module.condominium.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.module.condominium.dto.BuildingCreateRequest;
import com.convivium.module.condominium.dto.BuildingResponse;
import com.convivium.module.condominium.dto.CondominiumCreateRequest;
import com.convivium.module.condominium.dto.ApplyStructureRequest;
import com.convivium.module.condominium.dto.GenerateStructureRequest;
import com.convivium.module.condominium.dto.StructurePreviewResponse;
import com.convivium.module.condominium.dto.CondominiumResponse;
import com.convivium.module.condominium.dto.UnitCreateRequest;
import com.convivium.module.condominium.dto.UnitResidentInfo;
import com.convivium.module.condominium.dto.UnitResponse;
import com.convivium.module.condominium.entity.Building;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Plan;
import com.convivium.module.condominium.entity.Unit;
import com.convivium.module.condominium.repository.BuildingRepository;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.repository.PlanRepository;
import com.convivium.module.condominium.repository.UnitRepository;
import com.convivium.module.user.entity.UserCondominiumRole;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CondominiumService {

    private final CondominiumRepository condominiumRepository;
    private final PlanRepository planRepository;
    private final BuildingRepository buildingRepository;
    private final UnitRepository unitRepository;
    private final UserCondominiumRoleRepository userCondominiumRoleRepository;

    @Transactional(readOnly = true)
    public Page<CondominiumResponse> listAll(Pageable pageable) {
        return condominiumRepository.findAll(pageable)
                .map(this::mapToCondominiumResponse);
    }

    public CondominiumResponse create(CondominiumCreateRequest request) {
        String slug = generateSlug(request.name());

        if (condominiumRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        if (request.cnpj() != null && condominiumRepository.existsByCnpj(request.cnpj())) {
            throw new BusinessException("CNPJ ja cadastrado", "CNPJ_ALREADY_EXISTS");
        }

        Condominium condominium = Condominium.builder()
                .name(request.name())
                .slug(slug)
                .cnpj(request.cnpj())
                .email(request.email())
                .phone(request.phone())
                .addressStreet(request.addressStreet())
                .addressNumber(request.addressNumber())
                .addressComplement(request.addressComplement())
                .addressNeighborhood(request.addressNeighborhood())
                .addressCity(request.addressCity())
                .addressState(request.addressState())
                .addressZip(request.addressZip())
                .status("ACTIVE")
                .build();

        condominium = condominiumRepository.save(condominium);

        return mapToCondominiumResponse(condominium);
    }

    @Transactional(readOnly = true)
    public CondominiumResponse getById(Long id) {
        Condominium condominium = condominiumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", id));
        return mapToCondominiumResponse(condominium);
    }

    public CondominiumResponse update(Long id, CondominiumCreateRequest request) {
        Condominium condominium = condominiumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", id));

        condominium.setName(request.name());
        condominium.setCnpj(request.cnpj());
        condominium.setEmail(request.email());
        condominium.setPhone(request.phone());
        condominium.setAddressStreet(request.addressStreet());
        condominium.setAddressNumber(request.addressNumber());
        condominium.setAddressComplement(request.addressComplement());
        condominium.setAddressNeighborhood(request.addressNeighborhood());
        condominium.setAddressCity(request.addressCity());
        condominium.setAddressState(request.addressState());
        condominium.setAddressZip(request.addressZip());

        condominium = condominiumRepository.save(condominium);

        return mapToCondominiumResponse(condominium);
    }

    public void updateStatus(Long id, String status) {
        Condominium condominium = condominiumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", id));

        condominium.setStatus(status);
        condominiumRepository.save(condominium);
    }

    /** Associa um plano ao condominio (admin). */
    public CondominiumResponse setPlan(Long condominiumId, Long planId) {
        Condominium condominium = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condominiumId));

        Plan plan = null;
        if (planId != null) {
            plan = planRepository.findById(planId)
                    .orElseThrow(() -> new ResourceNotFoundException("Plano", planId));
        }
        condominium.setPlan(plan);
        condominium = condominiumRepository.save(condominium);
        return mapToCondominiumResponse(condominium);
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> listBuildings(Long condoId) {
        condominiumRepository.findById(condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condoId));

        return buildingRepository.findByCondominiumId(condoId).stream()
                .map(this::mapToBuildingResponse)
                .toList();
    }

    public BuildingResponse createBuilding(Long condoId, BuildingCreateRequest request) {
        condominiumRepository.findById(condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condoId));

        Building building = new Building();
        building.setName(request.name());
        building.setFloors(request.floors());
        building.setCondominiumId(condoId);

        building = buildingRepository.save(building);

        return mapToBuildingResponse(building);
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> listUnits(Long condoId) {
        condominiumRepository.findById(condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condoId));

        List<Unit> units = unitRepository.findByCondominiumId(condoId);
        List<UserCondominiumRole> withUnit = userCondominiumRoleRepository
                .findByCondominiumIdAndUnitNotNullAndStatus(condoId, "ACTIVE");
        Map<Long, List<UnitResidentInfo>> residentsByUnitId = withUnit.stream()
                .collect(Collectors.groupingBy(
                        ucr -> ucr.getUnit().getId(),
                        Collectors.mapping(
                                ucr -> new UnitResidentInfo(ucr.getUser().getName(), ucr.getUser().getEmail()),
                                Collectors.toList()
                        )
                ));

        return units.stream()
                .map(unit -> mapToUnitResponse(unit, residentsByUnitId.getOrDefault(unit.getId(), List.of())))
                .toList();
    }

    public UnitResponse createUnit(Long condoId, UnitCreateRequest request) {
        condominiumRepository.findById(condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condoId));

        Building building = null;
        if (request.buildingId() != null) {
            building = buildingRepository.findById(request.buildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bloco", request.buildingId()));

            if (!building.getCondominiumId().equals(condoId)) {
                throw new BusinessException("Bloco nao pertence a este condominio", "BUILDING_NOT_IN_CONDOMINIUM");
            }
        }

        Unit unit = Unit.builder()
                .condominiumId(condoId)
                .building(building)
                .identifier(request.identifier())
                .floor(request.floor())
                .type(request.type() != null ? request.type() : "APARTMENT")
                .areaSqm(request.areaSqm())
                .isOccupied(false)
                .build();

        unit = unitRepository.save(unit);

        return mapToUnitResponse(unit, List.of());
    }

    /**
     * Gera apenas o preview (lista de blocos e unidades) sem salvar no banco.
     * O admin pode revisar, editar e depois chamar applyStructure para salvar.
     */
    @Transactional(readOnly = true)
    public StructurePreviewResponse previewStructure(Long condoId, GenerateStructureRequest request) {
        condominiumRepository.findById(condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condoId));
        int blocksCount = request.blocksCount();
        int unitsPerFloor = request.unitsPerFloor();
        int floorsPerBlock = request.floorsPerBlock();
        String format = request.identifierFormat();
        int start = request.identifierStart() != null ? request.identifierStart() : 1;

        List<StructurePreviewResponse.BuildingPreviewItem> buildings = new ArrayList<>();
        List<StructurePreviewResponse.UnitPreviewItem> units = new ArrayList<>();

        for (int b = 0; b < blocksCount; b++) {
            String buildingName = String.valueOf((char) ('A' + b));
            buildings.add(new StructurePreviewResponse.BuildingPreviewItem(buildingName, floorsPerBlock));
            for (int floor = 1; floor <= floorsPerBlock; floor++) {
                for (int unitNum = 1; unitNum <= unitsPerFloor; unitNum++) {
                    String identifier = formatIdentifier(format, start, floor, unitNum, unitsPerFloor);
                    units.add(new StructurePreviewResponse.UnitPreviewItem(buildingName, floor, identifier));
                }
            }
        }
        return new StructurePreviewResponse(buildings, units);
    }

    private static String formatIdentifier(String format, int start, int floor, int unitNum, int unitsPerFloor) {
        return switch (format != null ? format : "101") {
            case "1" -> String.valueOf(start + (floor - 1) * unitsPerFloor + (unitNum - 1));
            case "01" -> {
                int n = start + (floor - 1) * unitsPerFloor + (unitNum - 1);
                yield n <= 99 ? String.format("%02d", n) : String.valueOf(n);
            }
            default -> String.valueOf(floor * 100 + (start + unitNum - 1)); // "101": andar + numero (ex: 111, 112 se start=11)
        };
    }

    /**
     * Salva no banco a estrutura revisada (blocos e unidades).
     * Blocos já existentes pelo nome são ignorados; unidades são criadas no bloco correspondente.
     */
    @Transactional
    public void applyStructure(Long condoId, ApplyStructureRequest request) {
        condominiumRepository.findById(condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condoId));

        Map<String, Building> nameToBuilding = new HashMap<>();
        for (StructurePreviewResponse.BuildingPreviewItem item : request.buildings()) {
            Building building = buildingRepository.findByCondominiumIdAndName(condoId, item.name())
                    .orElse(null);
            if (building == null) {
                building = new Building();
                building.setName(item.name());
                building.setFloors(item.floors());
                building.setCondominiumId(condoId);
                building = buildingRepository.save(building);
            }
            nameToBuilding.put(item.name(), building);
        }

        for (StructurePreviewResponse.UnitPreviewItem item : request.units()) {
            Building building = nameToBuilding.get(item.buildingName());
            if (building == null) continue;
            if (unitRepository.existsByCondominiumIdAndBuilding_IdAndIdentifier(condoId, building.getId(), item.identifier())) {
                continue; // unidade já existe (ex.: criada manualmente); ignora para não violar constraint única
            }
            Unit unit = Unit.builder()
                    .condominiumId(condoId)
                    .building(building)
                    .identifier(item.identifier())
                    .floor(item.floor())
                    .type("APARTMENT")
                    .areaSqm(null)
                    .isOccupied(false)
                    .build();
            unitRepository.save(unit);
        }
    }

    /**
     * Gera em lote blocos (A, B, C, ...) e unidades (salva direto no banco).
     * Se um bloco com o mesmo nome já existir no condomínio, esse bloco é ignorado.
     */
    @Transactional
    public void generateStructure(Long condoId, GenerateStructureRequest request) {
        condominiumRepository.findById(condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condoId));

        int blocksCount = request.blocksCount();
        int unitsPerFloor = request.unitsPerFloor();
        int floorsPerBlock = request.floorsPerBlock();
        String format = request.identifierFormat();
        int start = request.identifierStart() != null ? request.identifierStart() : 1;

        for (int b = 0; b < blocksCount; b++) {
            String buildingName = String.valueOf((char) ('A' + b));
            Building building = buildingRepository.findByCondominiumIdAndName(condoId, buildingName)
                    .orElse(null);
            if (building != null) {
                continue;
            }
            building = new Building();
            building.setName(buildingName);
            building.setFloors(floorsPerBlock);
            building.setCondominiumId(condoId);
            building = buildingRepository.save(building);

            for (int floor = 1; floor <= floorsPerBlock; floor++) {
                for (int unitNum = 1; unitNum <= unitsPerFloor; unitNum++) {
                    String identifier = formatIdentifier(format, start, floor, unitNum, unitsPerFloor);
                    Unit unit = Unit.builder()
                            .condominiumId(condoId)
                            .building(building)
                            .identifier(identifier)
                            .floor(floor)
                            .type("APARTMENT")
                            .areaSqm(null)
                            .isOccupied(false)
                            .build();
                    unitRepository.save(unit);
                }
            }
        }
    }

    public void deleteUnit(Long condoId, Long unitId) {
        condominiumRepository.findById(condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condoId));
        Unit unit = unitRepository.findByCondominiumIdAndId(condoId, unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade", unitId));
        if (userCondominiumRoleRepository.existsByUnit_Id(unit.getId())) {
            throw new BusinessException("Unidade em uso por morador. Remova o morador da unidade antes de excluir.", "UNIT_IN_USE");
        }
        unitRepository.delete(unit);
    }

    public void deleteBuilding(Long condoId, Long buildingId) {
        condominiumRepository.findById(condoId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condoId));
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Bloco", buildingId));
        if (!building.getCondominiumId().equals(condoId)) {
            throw new BusinessException("Bloco nao pertence a este condominio", "BUILDING_NOT_IN_CONDOMINIUM");
        }
        List<Unit> units = unitRepository.findByCondominiumIdAndBuilding_Id(condoId, buildingId);
        for (Unit unit : units) {
            if (userCondominiumRoleRepository.existsByUnit_Id(unit.getId())) {
                throw new BusinessException("Bloco possui unidades em uso. Remova os moradores antes de excluir o bloco.", "BUILDING_HAS_UNITS_IN_USE");
            }
        }
        for (Unit unit : units) {
            unitRepository.delete(unit);
        }
        buildingRepository.delete(building);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private CondominiumResponse mapToCondominiumResponse(Condominium condo) {
        Long planId = null;
        String planName = null;
        Integer planPriceCents = null;
        if (condo.getPlan() != null) {
            planId = condo.getPlan().getId();
            planName = condo.getPlan().getName();
            planPriceCents = condo.getPlan().getPriceCents();
        }
        return new CondominiumResponse(
                condo.getId(),
                condo.getName(),
                condo.getSlug(),
                condo.getCnpj(),
                condo.getEmail(),
                condo.getPhone(),
                condo.getAddressStreet(),
                condo.getAddressNumber(),
                condo.getAddressComplement(),
                condo.getAddressNeighborhood(),
                condo.getAddressCity(),
                condo.getAddressState(),
                condo.getAddressZip(),
                condo.getLogoUrl(),
                planId,
                planName,
                planPriceCents,
                condo.getStatus(),
                condo.getCreatedAt() != null ? condo.getCreatedAt().toString() : null
        );
    }

    private BuildingResponse mapToBuildingResponse(Building building) {
        return new BuildingResponse(
                building.getId(),
                building.getName(),
                building.getFloors(),
                building.getCreatedAt() != null ? building.getCreatedAt().toString() : null
        );
    }

    private UnitResponse mapToUnitResponse(Unit unit, List<UnitResidentInfo> residents) {
        Long buildingId = null;
        String buildingName = null;

        if (unit.getBuilding() != null) {
            buildingId = unit.getBuilding().getId();
            buildingName = unit.getBuilding().getName();
        }

        boolean occupied = residents != null && !residents.isEmpty()
                || userCondominiumRoleRepository.existsByUnit_Id(unit.getId());
        return new UnitResponse(
                unit.getId(),
                buildingId,
                buildingName,
                unit.getIdentifier(),
                unit.getFloor(),
                unit.getType(),
                unit.getAreaSqm(),
                occupied,
                residents != null ? residents : List.of()
        );
    }
}
