package com.hitachi.drone.repository;

import com.hitachi.drone.model.entity.DroneEntity;
import com.hitachi.drone.model.enums.DroneState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DroneRepository extends JpaRepository<DroneEntity, UUID> {
    Optional<DroneEntity> findBySerialNumber(String serialNumber);

    List<DroneEntity> findByStateIsNullOrState(DroneState state);
}
