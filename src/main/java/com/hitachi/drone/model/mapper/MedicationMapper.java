package com.hitachi.drone.model.mapper;

import com.hitachi.drone.model.domain.Medication;
import com.hitachi.drone.model.dto.MedicationRequest;
import com.hitachi.drone.model.entity.MedicationEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicationMapper {
    List<Medication> toDomain(List<MedicationRequest> requests);

    List<MedicationEntity> toEntity(List<Medication> medications);
}
