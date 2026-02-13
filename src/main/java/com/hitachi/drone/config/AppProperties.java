package com.hitachi.drone.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private DroneProperties drone;

    @Data
    public static class DroneProperties {
        private Integer maxDrones;
        private Double minBatteryForLoading;
        private Double batteryConsumptionPerDelivery;
        private Integer stateSchedulerFixedRate;
    }
}
