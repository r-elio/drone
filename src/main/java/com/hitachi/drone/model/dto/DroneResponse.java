package com.hitachi.drone.model.dto;

import com.hitachi.drone.model.enums.DroneModel;
import com.hitachi.drone.model.enums.DroneState;
import lombok.Builder;

import java.util.List;

@Builder
public record DroneResponse(
        String serialNumber,
        DroneModel model,
        Double batteryCapacity,
        DroneState state,
        List<MedicationResponse> medications,
        Double weightLimit,
        Double currentWeight) {
}
