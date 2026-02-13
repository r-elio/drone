package com.hitachi.drone.controller;

import com.hitachi.drone.model.domain.Drone;
import com.hitachi.drone.model.domain.Medication;
import com.hitachi.drone.model.dto.DroneResponse;
import com.hitachi.drone.model.dto.LoadMedicationRequest;
import com.hitachi.drone.model.dto.RegisterDroneRequest;
import com.hitachi.drone.model.enums.DroneState;
import com.hitachi.drone.model.mapper.DroneMapper;
import com.hitachi.drone.model.mapper.MedicationMapper;
import com.hitachi.drone.service.DroneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
@RequiredArgsConstructor
public class DroneController {

    private final DroneService droneService;

    private final DroneMapper droneMapper;
    private final MedicationMapper medicationMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DroneResponse registerDrone(@Valid @RequestBody RegisterDroneRequest request) {
        Drone drone = droneMapper.toDomain(request);
        return droneMapper.toDTO(droneService.registerDrone(drone));
    }

    @PostMapping("/{serialNumber}/medications")
    @ResponseStatus(HttpStatus.OK)
    public DroneResponse loadMedication(@PathVariable String serialNumber, @Validated @RequestBody LoadMedicationRequest request) {
        List<Medication> medications = medicationMapper.toDomain(request.medications());
        return droneMapper.toDTO(droneService.loadMedication(serialNumber, medications));
    }

    @GetMapping("/{serialNumber}")
    @ResponseStatus(HttpStatus.OK)
    public DroneResponse getDrone(@PathVariable String serialNumber) {
        return droneMapper.toDTO(droneService.getDrone(serialNumber));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<DroneResponse> getDrones(@RequestParam(required = false) DroneState state) {
        return droneMapper.toDTO(droneService.getDrones(state));
    }
}
