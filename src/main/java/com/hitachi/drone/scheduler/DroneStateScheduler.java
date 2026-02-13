package com.hitachi.drone.scheduler;

import com.hitachi.drone.config.AppProperties;
import com.hitachi.drone.model.entity.DroneEntity;
import com.hitachi.drone.model.enums.DroneState;
import com.hitachi.drone.repository.DroneRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DroneStateScheduler {

    private final DroneRepository droneRepository;
    private final AppProperties appProperties;

    @Scheduled(fixedRateString = "${app.drone.state-scheduler-fixed-rate}")
    @Transactional
    public void processDroneStates() {
        List<DroneEntity> drones = droneRepository.findAll();
        for (DroneEntity drone : drones) {
            switch (drone.getState()) {
                case LOADING -> {
                    log.info("Drone {} transitioning LOADING -> LOADED", drone.getSerialNumber());
                    log.trace(drone.toString());
                    drone.setState(DroneState.LOADED);
                }
                case LOADED -> {
                    log.info("Drone {} transitioning LOADED -> DELIVERING", drone.getSerialNumber());
                    log.trace(drone.toString());
                    drone.setState(DroneState.DELIVERING);
                }
                case DELIVERING -> {
                    log.info("Drone {} transitioning DELIVERING -> DELIVERED", drone.getSerialNumber());
                    log.trace(drone.toString());
                    drone.setState(DroneState.DELIVERED);
                }
                case DELIVERED -> {
                    log.info("Drone {} delivery completed. Reducing battery.", drone.getSerialNumber());
                    log.trace(drone.toString());
                    drone.setBatteryCapacity(Math.max(drone.getBatteryCapacity() - appProperties.getDrone().getBatteryConsumptionPerDelivery(), 0.0));
                    drone.setState(DroneState.RETURNING);
                }
                case RETURNING -> {
                    log.info("Drone {} returning to base. Clearing payload.", drone.getSerialNumber());
                    log.trace(drone.toString());
                    drone.getMedications().clear();
                    drone.setState(DroneState.IDLE);
                }
                default -> {
                }
            }
        }

        droneRepository.saveAll(drones);
    }
}
