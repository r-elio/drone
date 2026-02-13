package com.hitachi.drone.service.impl;

import com.hitachi.drone.config.AppProperties;
import com.hitachi.drone.model.domain.Drone;
import com.hitachi.drone.model.domain.Medication;
import com.hitachi.drone.model.entity.DroneEntity;
import com.hitachi.drone.model.entity.MedicationEntity;
import com.hitachi.drone.model.enums.DroneState;
import com.hitachi.drone.model.mapper.DroneMapper;
import com.hitachi.drone.model.mapper.MedicationMapper;
import com.hitachi.drone.repository.DroneRepository;
import com.hitachi.drone.service.DroneService;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DroneServiceImpl implements DroneService {

    private final DroneRepository droneRepository;
    private final DroneMapper droneMapper;
    private final MedicationMapper medicationMapper;
    private final AppProperties appProperties;

    @Override
    public Drone registerDrone(Drone drone) {
        if (drone.getSerialNumber() != null && droneRepository.findBySerialNumber(drone.getSerialNumber()).isPresent()) {
            throw new IllegalStateException("Drone with serial number already exists: " + drone.getSerialNumber());
        }

        if (droneRepository.count() >= appProperties.getDrone().getMaxDrones()) {
            throw new IllegalStateException("Total drones exceeds max limit: " + appProperties.getDrone().getMaxDrones());
        }

        return droneMapper.toDomain(droneRepository.save(droneMapper.toEntity(drone)));
    }

    @Override
    public Drone loadMedication(String serialNumber, List<Medication> medications) {
        DroneEntity drone = droneRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new EntityNotFoundException("Drone not found with serial number: " + serialNumber));

        if (drone.getState() == null || !drone.getState().equals(DroneState.IDLE)) {
            throw new IllegalStateException("Drone state must be IDLE to load medications: " + drone.getState());
        }

        if (drone.getBatteryCapacity() < appProperties.getDrone().getMinBatteryForLoading()) {
            throw new IllegalStateException("Drone battery too low to load medications: " + drone.getBatteryCapacity());
        }

        double totalNewWeight = medications.stream().mapToDouble(Medication::getWeight).sum();
        if (drone.getCurrentWeight() + totalNewWeight > drone.getWeightLimit()) {
            throw new IllegalStateException("Total weight exceeds drone weight limit: " + drone.getWeightLimit());
        }

        List<MedicationEntity> medicationEntities = medicationMapper.toEntity(medications);
        medicationEntities.forEach(entity -> entity.setDrone(drone));
        drone.getMedications().addAll(medicationEntities);
        drone.setState(DroneState.LOADING);

        return droneMapper.toDomain(droneRepository.save(drone));
    }

    @Override
    public Drone getDrone(String serialNumber) {
        return droneMapper.toDomain(droneRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new EntityNotFoundException("Drone not found with serial number: " + serialNumber)));
    }

    @Override
    public List<Drone> getDrones(@Nullable DroneState state) {
        return droneMapper.toDomain(droneRepository.findByStateIsNullOrState(state));
    }
}
