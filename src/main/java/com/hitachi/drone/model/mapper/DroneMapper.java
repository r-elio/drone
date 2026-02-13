package com.hitachi.drone.model.mapper;

import com.hitachi.drone.model.domain.Drone;
import com.hitachi.drone.model.dto.DroneResponse;
import com.hitachi.drone.model.dto.RegisterDroneRequest;
import com.hitachi.drone.model.entity.DroneEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MedicationMapper.class}, nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface DroneMapper {
    Drone toDomain(RegisterDroneRequest request);

    DroneResponse toDTO(Drone drone);

    List<DroneResponse> toDTO(List<Drone> drones);

    DroneEntity toEntity(Drone drone);

    Drone toDomain(DroneEntity entity);

    List<Drone> toDomain(List<DroneEntity> entities);

}
