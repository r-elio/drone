package com.hitachi.drone.model.domain;

import com.hitachi.drone.model.enums.DroneModel;
import com.hitachi.drone.model.enums.DroneState;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@ToString
@Builder
public class Drone {
    String serialNumber;
    DroneModel model;
    Double batteryCapacity;
    @Builder.Default
    DroneState state = DroneState.IDLE;
    @Builder.Default
    List<Medication> medications = new ArrayList<>();
    @Builder.Default
    Double weightLimit = 0.0;
    @Builder.Default
    Double currentWeight = 0.0;
}
