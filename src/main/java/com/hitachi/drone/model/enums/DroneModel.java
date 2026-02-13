package com.hitachi.drone.model.enums;

import lombok.Getter;

@Getter
public enum DroneModel {
    LIGHTWEIGHT(250.0),
    MIDDLEWEIGHT(500.0),
    CRUISERWEIGHT(750.0),
    HEAVYWEIGHT(1000.0);

    private final Double weightLimit;

    DroneModel(Double weightLimit) {
        this.weightLimit = weightLimit;
    }

}
