package com.hitachi.drone.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hitachi.drone.model.domain.Drone;
import com.hitachi.drone.model.domain.Medication;
import com.hitachi.drone.model.dto.DroneResponse;
import com.hitachi.drone.model.dto.LoadMedicationRequest;
import com.hitachi.drone.model.dto.MedicationRequest;
import com.hitachi.drone.model.dto.MedicationResponse;
import com.hitachi.drone.model.dto.RegisterDroneRequest;
import com.hitachi.drone.model.enums.DroneModel;
import com.hitachi.drone.model.enums.DroneState;
import com.hitachi.drone.model.mapper.DroneMapper;
import com.hitachi.drone.model.mapper.MedicationMapper;
import com.hitachi.drone.service.DroneService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DroneController.class)
@DisplayName("DroneController Tests")
class DroneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DroneService droneService;

    @MockitoBean
    private DroneMapper droneMapper;

    @MockitoBean
    private MedicationMapper medicationMapper;

    private RegisterDroneRequest registerDroneRequest;
    private DroneResponse droneResponse;
    private LoadMedicationRequest loadMedicationRequest;
    private Drone testDrone;
    private Medication testMedication;

    @BeforeEach
    void setUp() {
        registerDroneRequest = RegisterDroneRequest.builder()
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .build();

        testDrone = Drone.builder()
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .state(DroneState.IDLE)
                .weightLimit(1000.0)
                .currentWeight(0.0)
                .medications(new ArrayList<>())
                .build();

        droneResponse = DroneResponse.builder()
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .state(DroneState.IDLE)
                .weightLimit(1000.0)
                .currentWeight(0.0)
                .medications(new ArrayList<>())
                .build();

        testMedication = Medication.builder()
                .name("Medication-A")
                .code("MED_CODE_001")
                .weight(100.0)
                .image("https://example.com/med.jpg")
                .build();

        MedicationRequest medicationRequest = MedicationRequest.builder()
                .name("Medication-A")
                .code("MED_CODE_001")
                .weight(100.0)
                .image("https://example.com/med.jpg")
                .build();

        loadMedicationRequest = LoadMedicationRequest.builder()
                .medications(List.of(medicationRequest))
                .build();
    }

    @Test
    @DisplayName("POST /api/drones - Should register drone successfully and return 201")
    void testRegisterDrone_Success() throws Exception {
        when(droneMapper.toDomain(any(RegisterDroneRequest.class))).thenReturn(testDrone);
        when(droneService.registerDrone(any(Drone.class))).thenReturn(testDrone);
        when(droneMapper.toDTO(any(Drone.class))).thenReturn(droneResponse);

        ResultActions result = mockMvc.perform(post("/api/drones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDroneRequest)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.serial_number").value("DRONE-001"))
                .andExpect(jsonPath("$.model").value("HEAVYWEIGHT"))
                .andExpect(jsonPath("$.battery_capacity").value(100.0))
                .andExpect(jsonPath("$.state").value("IDLE"));

        verify(droneService).registerDrone(any(Drone.class));
    }

    @Test
    @DisplayName("POST /api/drones - Should return 400 when serial number is empty")
    void testRegisterDrone_EmptySerialNumber() throws Exception {
        RegisterDroneRequest request = RegisterDroneRequest.builder()
                .serialNumber("")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .build();

        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(droneService, times(0)).registerDrone(any());
    }

    @Test
    @DisplayName("POST /api/drones - Should return 400 when battery capacity exceeds 100")
    void testRegisterDrone_InvalidBatteryCapacity() throws Exception {
        RegisterDroneRequest request = RegisterDroneRequest.builder()
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(101.0)
                .build();

        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(droneService, times(0)).registerDrone(any());
    }

    @Test
    @DisplayName("POST /api/drones - Should return 400 when model is null")
    void testRegisterDrone_NullModel() throws Exception {
        RegisterDroneRequest request = RegisterDroneRequest.builder()
                .serialNumber("DRONE-001")
                .model(null)
                .batteryCapacity(100.0)
                .build();

        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(droneService, times(0)).registerDrone(any());
    }

    @Test
    @DisplayName("POST /api/drones - Should return 409 when drone serial number already exists")
    void testRegisterDrone_DuplicateSerialNumber() throws Exception {
        when(droneMapper.toDomain(any(RegisterDroneRequest.class))).thenReturn(testDrone);
        when(droneService.registerDrone(any(Drone.class)))
                .thenThrow(new IllegalStateException("Drone with serial number already exists: DRONE-001"));

        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDroneRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("Drone with serial number already exists")));

        verify(droneService).registerDrone(any(Drone.class));
    }

    @Test
    @DisplayName("GET /api/drones/{serialNumber} - Should retrieve drone successfully")
    void testGetDrone_Success() throws Exception {
        when(droneService.getDrone("DRONE-001")).thenReturn(testDrone);
        when(droneMapper.toDTO(any(Drone.class))).thenReturn(droneResponse);

        mockMvc.perform(get("/api/drones/DRONE-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serial_number").value("DRONE-001"))
                .andExpect(jsonPath("$.battery_capacity").value(100.0))
                .andExpect(jsonPath("$.state").value("IDLE"));

        verify(droneService).getDrone("DRONE-001");
    }

    @Test
    @DisplayName("GET /api/drones/{serialNumber} - Should return 404 when drone not found")
    void testGetDrone_NotFound() throws Exception {
        when(droneService.getDrone("INVALID-001"))
                .thenThrow(new EntityNotFoundException("Drone not found with serial number: INVALID-001"));

        mockMvc.perform(get("/api/drones/INVALID-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Drone not found")));

        verify(droneService).getDrone("INVALID-001");
    }

    @Test
    @DisplayName("GET /api/drones - Should retrieve all drones successfully")
    void testGetDrones_All() throws Exception {
        List<Drone> drones = List.of(testDrone);
        List<DroneResponse> responses = List.of(droneResponse);
        when(droneService.getDrones(null)).thenReturn(drones);
        when(droneMapper.toDTO(anyList())).thenReturn(responses);

        mockMvc.perform(get("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serial_number").value("DRONE-001"))
                .andExpect(jsonPath("$[0].state").value("IDLE"));

        verify(droneService).getDrones(null);
    }

    @Test
    @DisplayName("GET /api/drones?state=IDLE - Should retrieve drones by state")
    void testGetDrones_ByState() throws Exception {
        List<Drone> drones = List.of(testDrone);
        List<DroneResponse> responses = List.of(droneResponse);
        when(droneService.getDrones(DroneState.IDLE)).thenReturn(drones);
        when(droneMapper.toDTO(anyList())).thenReturn(responses);

        mockMvc.perform(get("/api/drones")
                        .param("state", "IDLE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].state").value("IDLE"));

        verify(droneService).getDrones(DroneState.IDLE);
    }

    @Test
    @DisplayName("GET /api/drones - Should return empty list when no drones found")
    void testGetDrones_Empty() throws Exception {
        when(droneService.getDrones(null)).thenReturn(new ArrayList<>());
        when(droneMapper.toDTO(anyList())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(droneService).getDrones(null);
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should load medication successfully")
    void testLoadMedication_Success() throws Exception {
        Drone droneAfterLoad = Drone.builder()
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .state(DroneState.LOADING)
                .weightLimit(1000.0)
                .currentWeight(100.0)
                .medications(List.of(testMedication))
                .build();

        DroneResponse responseAfterLoad = DroneResponse.builder()
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .state(DroneState.LOADING)
                .weightLimit(1000.0)
                .currentWeight(100.0)
                .medications(List.of(
                        MedicationResponse.builder()
                                .name("Medication-A")
                                .code("MED_CODE_001")
                                .weight(100.0)
                                .image("https://example.com/med.jpg")
                                .build()
                ))
                .build();

        when(medicationMapper.toDomain(any())).thenReturn(List.of(testMedication));
        when(droneService.loadMedication(eq("DRONE-001"), anyList())).thenReturn(droneAfterLoad);
        when(droneMapper.toDTO(any(Drone.class))).thenReturn(responseAfterLoad);

        mockMvc.perform(post("/api/drones/DRONE-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadMedicationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serial_number").value("DRONE-001"))
                .andExpect(jsonPath("$.state").value("LOADING"))
                .andExpect(jsonPath("$.current_weight").value(100.0))
                .andExpect(jsonPath("$.medications.length()").value(1));

        verify(droneService).loadMedication(eq("DRONE-001"), anyList());
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should return 400 when medications list is empty")
    void testLoadMedication_EmptyMedicationsList() throws Exception {
        LoadMedicationRequest request = LoadMedicationRequest.builder()
                .medications(new ArrayList<>())
                .build();

        mockMvc.perform(post("/api/drones/DRONE-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(droneService, times(0)).loadMedication(any(), anyList());
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should return 400 when medication name contains invalid characters")
    void testLoadMedication_InvalidMedicationName() throws Exception {
        MedicationRequest invalidRequest = MedicationRequest.builder()
                .name("Invalid!Name")
                .code("MED_CODE_001")
                .weight(100.0)
                .image("https://example.com/med.jpg")
                .build();

        LoadMedicationRequest request = LoadMedicationRequest.builder()
                .medications(List.of(invalidRequest))
                .build();

        mockMvc.perform(post("/api/drones/DRONE-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(droneService, times(0)).loadMedication(any(), anyList());
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should return 400 when medication code contains invalid characters")
    void testLoadMedication_InvalidMedicationCode() throws Exception {
        MedicationRequest invalidRequest = MedicationRequest.builder()
                .name("Medication-A")
                .code("med_code_001") // lowercase not allowed
                .weight(100.0)
                .image("https://example.com/med.jpg")
                .build();

        LoadMedicationRequest request = LoadMedicationRequest.builder()
                .medications(List.of(invalidRequest))
                .build();

        mockMvc.perform(post("/api/drones/DRONE-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(droneService, times(0)).loadMedication(any(), anyList());
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should return 400 when medication weight is negative")
    void testLoadMedication_NegativeWeight() throws Exception {
        MedicationRequest invalidRequest = MedicationRequest.builder()
                .name("Medication-A")
                .code("MED_CODE_001")
                .weight(-50.0)
                .image("https://example.com/med.jpg")
                .build();

        LoadMedicationRequest request = LoadMedicationRequest.builder()
                .medications(List.of(invalidRequest))
                .build();

        mockMvc.perform(post("/api/drones/DRONE-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(droneService, times(0)).loadMedication(any(), anyList());
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should return 400 when image URL is invalid")
    void testLoadMedication_InvalidImageUrl() throws Exception {
        MedicationRequest invalidRequest = MedicationRequest.builder()
                .name("Medication-A")
                .code("MED_CODE_001")
                .weight(100.0)
                .image("not-a-url")
                .build();

        LoadMedicationRequest request = LoadMedicationRequest.builder()
                .medications(List.of(invalidRequest))
                .build();

        mockMvc.perform(post("/api/drones/DRONE-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(droneService, times(0)).loadMedication(any(), anyList());
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should return 404 when drone not found")
    void testLoadMedication_DroneNotFound() throws Exception {
        when(medicationMapper.toDomain(any())).thenReturn(List.of(testMedication));
        when(droneService.loadMedication(eq("INVALID-001"), anyList()))
                .thenThrow(new EntityNotFoundException("Drone not found with serial number: INVALID-001"));

        mockMvc.perform(post("/api/drones/INVALID-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadMedicationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Drone not found")));

        verify(droneService).loadMedication(eq("INVALID-001"), anyList());
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should return 409 when drone battery too low")
    void testLoadMedication_LowBattery() throws Exception {
        when(medicationMapper.toDomain(any())).thenReturn(List.of(testMedication));
        when(droneService.loadMedication(eq("DRONE-001"), anyList()))
                .thenThrow(new IllegalStateException("Drone battery too low to load medications: 20.0"));

        mockMvc.perform(post("/api/drones/DRONE-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadMedicationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("battery too low")));

        verify(droneService).loadMedication(eq("DRONE-001"), anyList());
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should return 409 when weight exceeds capacity")
    void testLoadMedication_WeightExceedsCapacity() throws Exception {
        when(medicationMapper.toDomain(any())).thenReturn(List.of(testMedication));
        when(droneService.loadMedication(eq("DRONE-001"), anyList()))
                .thenThrow(new IllegalStateException("Total weight exceeds drone weight limit: 1000.0"));

        mockMvc.perform(post("/api/drones/DRONE-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadMedicationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("weight exceeds")));

        verify(droneService).loadMedication(eq("DRONE-001"), anyList());
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should return 409 when drone not idle")
    void testLoadMedication_DroneNotIdle() throws Exception {
        when(medicationMapper.toDomain(any())).thenReturn(List.of(testMedication));
        when(droneService.loadMedication(eq("DRONE-001"), anyList()))
                .thenThrow(new IllegalStateException("Drone state must be IDLE to load medications: LOADING"));

        mockMvc.perform(post("/api/drones/DRONE-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadMedicationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("state must be IDLE")));

        verify(droneService).loadMedication(eq("DRONE-001"), anyList());
    }

    @Test
    @DisplayName("POST /api/drones/{serialNumber}/medications - Should load multiple medications")
    void testLoadMedication_MultipleMedications() throws Exception {
        MedicationRequest med1 = MedicationRequest.builder()
                .name("Med-1")
                .code("MED_001")
                .weight(200.0)
                .image("https://example.com/med1.jpg")
                .build();

        MedicationRequest med2 = MedicationRequest.builder()
                .name("Med-2")
                .code("MED_002")
                .weight(300.0)
                .image("https://example.com/med2.jpg")
                .build();

        LoadMedicationRequest request = LoadMedicationRequest.builder()
                .medications(List.of(med1, med2))
                .build();

        Drone droneAfterLoad = Drone.builder()
                .serialNumber("DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .batteryCapacity(100.0)
                .state(DroneState.LOADING)
                .weightLimit(1000.0)
                .currentWeight(500.0)
                .medications(List.of(
                        Medication.builder().name("Med-1").code("MED_001").weight(200.0).image("https://example.com/med1.jpg").build(),
                        Medication.builder().name("Med-2").code("MED_002").weight(300.0).image("https://example.com/med2.jpg").build()
                ))
                .build();

        when(medicationMapper.toDomain(any())).thenReturn(droneAfterLoad.getMedications());
        when(droneService.loadMedication(eq("DRONE-001"), anyList())).thenReturn(droneAfterLoad);
        when(droneMapper.toDTO(any(Drone.class))).thenReturn(
                DroneResponse.builder()
                        .serialNumber("DRONE-001")
                        .model(DroneModel.HEAVYWEIGHT)
                        .batteryCapacity(100.0)
                        .state(DroneState.LOADING)
                        .weightLimit(1000.0)
                        .currentWeight(500.0)
                        .medications(List.of(
                                MedicationResponse.builder().name("Med-1").code("MED_001").weight(200.0).image("https://example.com/med1.jpg").build(),
                                MedicationResponse.builder().name("Med-2").code("MED_002").weight(300.0).image("https://example.com/med2.jpg").build()
                        ))
                        .build()
        );

        mockMvc.perform(post("/api/drones/DRONE-001/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.current_weight").value(500.0))
                .andExpect(jsonPath("$.medications.length()").value(2));

        verify(droneService).loadMedication(eq("DRONE-001"), anyList());
    }
}
