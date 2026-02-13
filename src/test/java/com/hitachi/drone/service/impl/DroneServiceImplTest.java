package com.hitachi.drone.service.impl;

import com.hitachi.drone.config.AppProperties;
import com.hitachi.drone.model.domain.Drone;
import com.hitachi.drone.model.domain.Medication;
import com.hitachi.drone.model.entity.DroneEntity;
import com.hitachi.drone.model.entity.MedicationEntity;
import com.hitachi.drone.model.enums.DroneModel;
import com.hitachi.drone.model.enums.DroneState;
import com.hitachi.drone.model.mapper.DroneMapper;
import com.hitachi.drone.model.mapper.MedicationMapper;
import com.hitachi.drone.repository.DroneRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DroneServiceImpl Tests")
class DroneServiceImplTest {

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private DroneMapper droneMapper;

    @Mock
    private MedicationMapper medicationMapper;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private DroneServiceImpl droneService;

    private AppProperties.DroneProperties droneProperties;
    private Drone testDrone;
    private DroneEntity testDroneEntity;
    private Medication testMedication;
    private MedicationEntity testMedicationEntity;

    @BeforeEach
    void setUp() {
        droneProperties = new AppProperties.DroneProperties();
        droneProperties.setMaxDrones(10);
        droneProperties.setMinBatteryForLoading(25.0);
        droneProperties.setBatteryConsumptionPerDelivery(10.0);

        testDrone = Drone.builder()
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .state(DroneState.IDLE)
                .weightLimit(1000.0)
                .currentWeight(0.0)
                .medications(new ArrayList<>())
                .build();

        testDroneEntity = DroneEntity.builder()
                .id(UUID.randomUUID())
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .state(DroneState.IDLE)
                .medications(new ArrayList<>())
                .build();

        testMedication = Medication.builder()
                .name("Medication-A")
                .code("MED_CODE_001")
                .weight(100.0)
                .image("https://example.com/med.jpg")
                .build();

        testMedicationEntity = MedicationEntity.builder()
                .id(UUID.randomUUID())
                .name("Medication-A")
                .code("MED_CODE_001")
                .weight(100.0)
                .image("https://example.com/med.jpg")
                .build();
    }

    @Test
    @DisplayName("Should register a new drone successfully")
    void testRegisterDrone_Success() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.empty());
        when(droneRepository.count()).thenReturn(0L);
        when(droneMapper.toEntity(any(Drone.class))).thenReturn(testDroneEntity);
        when(droneRepository.save(any(DroneEntity.class))).thenReturn(testDroneEntity);
        when(droneMapper.toDomain(any(DroneEntity.class))).thenReturn(testDrone);

        Drone result = droneService.registerDrone(testDrone);

        assertThat(result).isNotNull();
        assertThat(result.getSerialNumber()).isEqualTo("DRONE-001");
        assertThat(result.getModel()).isEqualTo(DroneModel.HEAVYWEIGHT);
        assertThat(result.getBatteryCapacity()).isEqualTo(100.0);
        assertThat(result.getState()).isEqualTo(DroneState.IDLE);
        verify(droneRepository).save(any(DroneEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when registering drone with duplicate serial number")
    void testRegisterDrone_DuplicateSerialNumber() {
        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));

        assertThatThrownBy(() -> droneService.registerDrone(testDrone))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Drone with serial number already exists");

        verify(droneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when registering drone exceeds max limit")
    void testRegisterDrone_ExceedsMaxLimit() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.empty());
        when(droneRepository.count()).thenReturn(10L);

        assertThatThrownBy(() -> droneService.registerDrone(testDrone))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Total drones exceeds max limit: 10");

        verify(droneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should register drone when count is below max limit")
    void testRegisterDrone_BelowMaxLimit() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.empty());
        when(droneRepository.count()).thenReturn(9L);
        when(droneMapper.toEntity(any(Drone.class))).thenReturn(testDroneEntity);
        when(droneRepository.save(any(DroneEntity.class))).thenReturn(testDroneEntity);
        when(droneMapper.toDomain(any(DroneEntity.class))).thenReturn(testDrone);

        Drone result = droneService.registerDrone(testDrone);

        assertThat(result).isNotNull();
        verify(droneRepository).save(any());
    }

    @Test
    @DisplayName("Should retrieve drone by serial number successfully")
    void testGetDrone_Success() {
        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));
        when(droneMapper.toDomain(any(DroneEntity.class))).thenReturn(testDrone);

        Drone result = droneService.getDrone("DRONE-001");

        assertThat(result).isNotNull();
        assertThat(result.getSerialNumber()).isEqualTo("DRONE-001");
        verify(droneRepository).findBySerialNumber("DRONE-001");
    }

    @Test
    @DisplayName("Should throw exception when drone not found")
    void testGetDrone_NotFound() {
        when(droneRepository.findBySerialNumber("INVALID-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> droneService.getDrone("INVALID-001"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Drone not found with serial number");
    }

    @Test
    @DisplayName("Should retrieve all drones when state is null")
    void testGetDrones_AllDrones() {
        List<DroneEntity> droneEntities = List.of(testDroneEntity);
        List<Drone> drones = List.of(testDrone);
        when(droneRepository.findByStateIsNullOrState(null)).thenReturn(droneEntities);
        when(droneMapper.toDomain(anyList())).thenReturn(drones);

        List<Drone> result = droneService.getDrones(null);

        assertThat(result).isNotNull().hasSize(1);
        verify(droneRepository).findByStateIsNullOrState(null);
    }

    @Test
    @DisplayName("Should retrieve drones by specific state")
    void testGetDrones_ByState() {
        testDroneEntity.setState(DroneState.IDLE);
        List<DroneEntity> droneEntities = List.of(testDroneEntity);
        List<Drone> drones = List.of(testDrone);
        when(droneRepository.findByStateIsNullOrState(DroneState.IDLE)).thenReturn(droneEntities);
        when(droneMapper.toDomain(anyList())).thenReturn(drones);

        List<Drone> result = droneService.getDrones(DroneState.IDLE);

        assertThat(result).isNotNull().hasSize(1);
        verify(droneRepository).findByStateIsNullOrState(DroneState.IDLE);
    }

    @Test
    @DisplayName("Should return empty list when no drones found")
    void testGetDrones_EmptyResult() {
        when(droneRepository.findByStateIsNullOrState(DroneState.DELIVERING)).thenReturn(new ArrayList<>());
        when(droneMapper.toDomain(anyList())).thenReturn(new ArrayList<>());

        List<Drone> result = droneService.getDrones(DroneState.DELIVERING);

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Should load medication to idle drone successfully")
    void testLoadMedication_Success() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        testDroneEntity.setState(DroneState.IDLE);
        List<Medication> medications = List.of(testMedication);
        List<MedicationEntity> medicationEntities = List.of(testMedicationEntity);

        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));
        when(medicationMapper.toEntity(any())).thenReturn(medicationEntities);
        when(droneRepository.save(any(DroneEntity.class))).thenReturn(testDroneEntity);
        when(droneMapper.toDomain(any(DroneEntity.class))).thenReturn(testDrone);

        Drone result = droneService.loadMedication("DRONE-001", medications);

        assertThat(result).isNotNull();
        assertThat(testDroneEntity.getState()).isEqualTo(DroneState.LOADING);
        verify(droneRepository).save(any(DroneEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when loading medication to non-existent drone")
    void testLoadMedication_DroneNotFound() {
        List<Medication> medications = List.of(testMedication);
        when(droneRepository.findBySerialNumber("INVALID-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> droneService.loadMedication("INVALID-001", medications))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Drone not found with serial number");

        verify(droneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when drone state is not IDLE")
    void testLoadMedication_DroneNotIdle() {
        testDroneEntity.setState(DroneState.LOADING);
        List<Medication> medications = List.of(testMedication);
        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));

        assertThatThrownBy(() -> droneService.loadMedication("DRONE-001", medications))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Drone state must be IDLE to load medications");

        verify(droneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when drone battery is below minimum threshold")
    void testLoadMedication_LowBattery() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        testDroneEntity.setState(DroneState.IDLE);
        testDroneEntity.setBatteryCapacity(20.0); // Below 25% threshold
        List<Medication> medications = List.of(testMedication);
        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));

        assertThatThrownBy(() -> droneService.loadMedication("DRONE-001", medications))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Drone battery too low to load medications");

        verify(droneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when total weight exceeds drone capacity")
    void testLoadMedication_WeightExceedsCapacity() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        testDroneEntity.setState(DroneState.IDLE);
        testDroneEntity.setBatteryCapacity(100.0);

        Medication heavyMedication = Medication.builder()
                .name("Heavy-Medication")
                .code("HEAVY_001")
                .weight(1001.0) // Exceeds 1000g capacity
                .image("https://example.com/heavy.jpg")
                .build();

        List<Medication> medications = List.of(heavyMedication);
        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));

        assertThatThrownBy(() -> droneService.loadMedication("DRONE-001", medications))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Total weight exceeds drone weight limit");

        verify(droneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when accumulated weight exceeds capacity")
    void testLoadMedication_AccumulatedWeightExceedsCapacity() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        testDroneEntity.setState(DroneState.IDLE);
        testDroneEntity.setBatteryCapacity(100.0);

        MedicationEntity existingMed = MedicationEntity.builder()
                .id(UUID.randomUUID())
                .name("Existing-Med")
                .code("EXIST_001")
                .weight(900.0)
                .image("https://example.com/exist.jpg")
                .build();
        testDroneEntity.getMedications().add(existingMed);

        Medication newMedication = Medication.builder()
                .name("New-Medication")
                .code("NEW_001")
                .weight(150.0) .image("https://example.com/new.jpg")
                .build();

        List<Medication> medications = List.of(newMedication);
        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));

        assertThatThrownBy(() -> droneService.loadMedication("DRONE-001", medications))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Total weight exceeds drone weight limit");

        verify(droneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should load multiple medications successfully")
    void testLoadMedication_MultipleMedications() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        testDroneEntity.setState(DroneState.IDLE);

        Medication med1 = Medication.builder()
                .name("Med-1")
                .code("MED_001")
                .weight(200.0)
                .image("https://example.com/med1.jpg")
                .build();

        Medication med2 = Medication.builder()
                .name("Med-2")
                .code("MED_002")
                .weight(300.0)
                .image("https://example.com/med2.jpg")
                .build();

        List<Medication> medications = List.of(med1, med2);
        List<MedicationEntity> medicationEntities = List.of(
                MedicationEntity.builder().id(UUID.randomUUID()).name("Med-1").code("MED_001").weight(200.0).image("https://example.com/med1.jpg").build(),
                MedicationEntity.builder().id(UUID.randomUUID()).name("Med-2").code("MED_002").weight(300.0).image("https://example.com/med2.jpg").build()
        );

        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));
        when(medicationMapper.toEntity(any())).thenReturn(medicationEntities);
        when(droneRepository.save(any(DroneEntity.class))).thenReturn(testDroneEntity);
        when(droneMapper.toDomain(any(DroneEntity.class))).thenReturn(testDrone);

        Drone result = droneService.loadMedication("DRONE-001", medications);

        assertThat(result).isNotNull();
        assertThat(testDroneEntity.getState()).isEqualTo(DroneState.LOADING);
        verify(droneRepository).save(any(DroneEntity.class));
    }

    @Test
    @DisplayName("Should load medication at exact battery capacity threshold")
    void testLoadMedication_AtMinBatteryThreshold() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        testDroneEntity.setState(DroneState.IDLE);
        testDroneEntity.setBatteryCapacity(25.0); // Exactly at threshold
        List<Medication> medications = List.of(testMedication);
        List<MedicationEntity> medicationEntities = List.of(testMedicationEntity);

        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));
        when(medicationMapper.toEntity(any())).thenReturn(medicationEntities);
        when(droneRepository.save(any(DroneEntity.class))).thenReturn(testDroneEntity);
        when(droneMapper.toDomain(any(DroneEntity.class))).thenReturn(testDrone);

        Drone result = droneService.loadMedication("DRONE-001", medications);

        assertThat(result).isNotNull();
        verify(droneRepository).save(any(DroneEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when drone state is null")
    void testLoadMedication_NullState() {
        testDroneEntity.setState(null);
        List<Medication> medications = List.of(testMedication);
        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));

        assertThatThrownBy(() -> droneService.loadMedication("DRONE-001", medications))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Drone state must be IDLE to load medications");

        verify(droneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update drone state to LOADING after successful load")
    void testLoadMedication_StateTransition() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        testDroneEntity.setState(DroneState.IDLE);
        List<Medication> medications = List.of(testMedication);
        List<MedicationEntity> medicationEntities = List.of(testMedicationEntity);

        when(droneRepository.findBySerialNumber("DRONE-001")).thenReturn(Optional.of(testDroneEntity));
        when(medicationMapper.toEntity(any())).thenReturn(medicationEntities);
        when(droneRepository.save(any(DroneEntity.class)))
                .thenAnswer(invocation -> invocation.<DroneEntity>getArgument(0));
        when(droneMapper.toDomain(any(DroneEntity.class))).thenReturn(testDrone);

        droneService.loadMedication("DRONE-001", medications);

        assertThat(testDroneEntity.getState()).isEqualTo(DroneState.LOADING);
    }
}
