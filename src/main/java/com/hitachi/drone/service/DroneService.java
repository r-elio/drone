package com.hitachi.drone.service;

import com.hitachi.drone.model.domain.Drone;
import com.hitachi.drone.model.domain.Medication;
import com.hitachi.drone.model.enums.DroneState;

import java.util.List;

public interface DroneService {
    Drone registerDrone(Drone drone);

    Drone getDrone(String serialNumber);

    List<Drone> getDrones(DroneState state);

    Drone loadMedication(String serialNumber, List<Medication> medications);
}
