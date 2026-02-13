package com.hitachi.drone.scheduler;

import com.hitachi.drone.config.AppProperties;
import com.hitachi.drone.model.entity.DroneEntity;
import com.hitachi.drone.model.entity.MedicationEntity;
import com.hitachi.drone.model.enums.DroneModel;
import com.hitachi.drone.model.enums.DroneState;
import com.hitachi.drone.repository.DroneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DroneStateScheduler Tests")
class DroneStateSchedulerTest {

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private DroneStateScheduler droneStateScheduler;

    private AppProperties.DroneProperties droneProperties;
    private DroneEntity testDrone;

    @BeforeEach
    void setUp() {
        droneProperties = new AppProperties.DroneProperties();
        droneProperties.setMaxDrones(10);
        droneProperties.setMinBatteryForLoading(25.0);
        droneProperties.setBatteryConsumptionPerDelivery(10.0);
        droneProperties.setStateSchedulerFixedRate(10000);

        testDrone = DroneEntity.builder()
                .id(UUID.randomUUID())
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .state(DroneState.IDLE)
                .medications(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should transition drone from LOADING to LOADED")
    void testProcessDroneStates_LoadingToLoaded() {
        testDrone.setState(DroneState.LOADING);
        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getState()).isEqualTo(DroneState.LOADED);
        verify(droneRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should transition drone from LOADED to DELIVERING")
    void testProcessDroneStates_LoadedToDelivering() {
        testDrone.setState(DroneState.LOADED);
        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getState()).isEqualTo(DroneState.DELIVERING);
        verify(droneRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should transition drone from DELIVERING to DELIVERED")
    void testProcessDroneStates_DeliveringToDelivered() {
        testDrone.setState(DroneState.DELIVERING);
        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getState()).isEqualTo(DroneState.DELIVERED);
        verify(droneRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should transition drone from DELIVERED to RETURNING and reduce battery")
    void testProcessDroneStates_DeliveredToReturning() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        testDrone.setState(DroneState.DELIVERED);
        testDrone.setBatteryCapacity(100.0);
        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getState()).isEqualTo(DroneState.RETURNING);
        assertThat(testDrone.getBatteryCapacity()).isEqualTo(90.0); // 100 - 10
        verify(droneRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should transition drone from RETURNING to IDLE and clear medications")
    void testProcessDroneStates_ReturningToIdle() {
        testDrone.setState(DroneState.RETURNING);
        MedicationEntity medication = MedicationEntity.builder()
                .id(UUID.randomUUID())
                .name("Medication-A")
                .code("MED_001")
                .weight(100.0)
                .image("https://example.com/med.jpg")
                .build();
        testDrone.getMedications().add(medication);

        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getState()).isEqualTo(DroneState.IDLE);
        assertThat(testDrone.getMedications()).isEmpty();
        verify(droneRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should not change state for IDLE drone")
    void testProcessDroneStates_IdleDroneUnchanged() {
        testDrone.setState(DroneState.IDLE);
        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getState()).isEqualTo(DroneState.IDLE);
        verify(droneRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reduce battery during DELIVERED to RETURNING transition")
    void testBatteryConsumption_StandardReduction() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        testDrone.setState(DroneState.DELIVERED);
        testDrone.setBatteryCapacity(50.0);
        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getBatteryCapacity()).isEqualTo(40.0); // 50 - 10
    }

    @Test
    @DisplayName("Should not let battery go below zero")
    void testBatteryConsumption_BatteryNotBelowZero() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        testDrone.setState(DroneState.DELIVERED);
        testDrone.setBatteryCapacity(5.0); // Less than battery consumption
        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getBatteryCapacity()).isEqualTo(0.0); // Math.max(5 - 10, 0)
    }

    @Test
    @DisplayName("Should use configured battery consumption rate")
    void testBatteryConsumption_ConfiguredRate() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        droneProperties.setBatteryConsumptionPerDelivery(15.0);
        testDrone.setState(DroneState.DELIVERED);
        testDrone.setBatteryCapacity(100.0);
        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getBatteryCapacity()).isEqualTo(85.0); // 100 - 15
    }

    @Test
    @DisplayName("Should clear medications when transitioning to IDLE")
    void testMedicationClearing_SingleMedication() {
        testDrone.setState(DroneState.RETURNING);
        MedicationEntity medication = MedicationEntity.builder()
                .id(UUID.randomUUID())
                .name("Medication-A")
                .code("MED_001")
                .weight(100.0)
                .image("https://example.com/med.jpg")
                .build();
        testDrone.getMedications().add(medication);

        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getMedications()).isEmpty();
        verify(droneRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should clear multiple medications when transitioning to IDLE")
    void testMedicationClearing_MultipleMedications() {
        testDrone.setState(DroneState.RETURNING);
        testDrone.getMedications().add(
                MedicationEntity.builder()
                        .id(UUID.randomUUID())
                        .name("Med-1")
                        .code("MED_001")
                        .weight(100.0)
                        .image("https://example.com/med1.jpg")
                        .build()
        );
        testDrone.getMedications().add(
                MedicationEntity.builder()
                        .id(UUID.randomUUID())
                        .name("Med-2")
                        .code("MED_002")
                        .weight(200.0)
                        .image("https://example.com/med2.jpg")
                        .build()
        );

        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(testDrone.getMedications()).isEmpty();
    }

    @Test
    @DisplayName("Should process multiple drones in different states")
    void testProcessDroneStates_MultipleDrones() {
        DroneEntity drone1 = DroneEntity.builder()
                .id(UUID.randomUUID())
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .state(DroneState.LOADING)
                .medications(new ArrayList<>())
                .build();

        DroneEntity drone2 = DroneEntity.builder()
                .id(UUID.randomUUID())
                .serialNumber("DRONE-002")
                .model(DroneModel.LIGHTWEIGHT)
                .batteryCapacity(80.0)
                .state(DroneState.LOADED)
                .medications(new ArrayList<>())
                .build();

        DroneEntity drone3 = DroneEntity.builder()
                .id(UUID.randomUUID())
                .serialNumber("DRONE-003")
                .model(DroneModel.MIDDLEWEIGHT)
                .batteryCapacity(60.0)
                .state(DroneState.DELIVERING)
                .medications(new ArrayList<>())
                .build();

        List<DroneEntity> drones = List.of(drone1, drone2, drone3);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        assertThat(drone1.getState()).isEqualTo(DroneState.LOADED);
        assertThat(drone2.getState()).isEqualTo(DroneState.DELIVERING);
        assertThat(drone3.getState()).isEqualTo(DroneState.DELIVERED);
        verify(droneRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle empty drone list")
    void testProcessDroneStates_EmptyList() {
        when(droneRepository.findAll()).thenReturn(new ArrayList<>());

        droneStateScheduler.processDroneStates();

        verify(droneRepository).findAll();
        verify(droneRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should save all updated drones after processing")
    void testProcessDroneStates_SaveAll() {
        List<DroneEntity> drones = List.of(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        droneStateScheduler.processDroneStates();

        ArgumentCaptor<List<DroneEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(droneRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("Should handle drone state transitions in sequence")
    void testProcessDroneStates_FullCycle() {
        when(appProperties.getDrone()).thenReturn(droneProperties);
        DroneEntity drone = DroneEntity.builder()
                .id(UUID.randomUUID())
                .serialNumber("DRONE-CYCLE")
                .model(DroneModel.CRUISERWEIGHT)
                .batteryCapacity(100.0)
                .state(DroneState.LOADING)
                .medications(new ArrayList<>())
                .build();

        MedicationEntity med = MedicationEntity.builder()
                .id(UUID.randomUUID())
                .name("Med")
                .code("MED_001")
                .weight(100.0)
                .image("https://example.com/med.jpg")
                .build();
        drone.getMedications().add(med);

        List<DroneEntity> drones = List.of(drone);
        when(droneRepository.findAll()).thenReturn(drones);

        // LOADING -> LOADED
        droneStateScheduler.processDroneStates();
        assertThat(drone.getState()).isEqualTo(DroneState.LOADED);
        assertThat(drone.getMedications()).isNotEmpty();

        // LOADED -> DELIVERING
        when(droneRepository.findAll()).thenReturn(drones);
        droneStateScheduler.processDroneStates();
        assertThat(drone.getState()).isEqualTo(DroneState.DELIVERING);

        // DELIVERING -> DELIVERED
        when(droneRepository.findAll()).thenReturn(drones);
        droneStateScheduler.processDroneStates();
        assertThat(drone.getState()).isEqualTo(DroneState.DELIVERED);

        // DELIVERED -> RETURNING (battery reduced)
        double batteryBefore = drone.getBatteryCapacity();
        when(droneRepository.findAll()).thenReturn(drones);
        droneStateScheduler.processDroneStates();
        assertThat(drone.getState()).isEqualTo(DroneState.RETURNING);
        assertThat(drone.getBatteryCapacity()).isEqualTo(batteryBefore - 10.0);

        // RETURNING -> IDLE (medications cleared)
        when(droneRepository.findAll()).thenReturn(drones);
        droneStateScheduler.processDroneStates();
        assertThat(drone.getState()).isEqualTo(DroneState.IDLE);
        assertThat(drone.getMedications()).isEmpty();
    }
}
