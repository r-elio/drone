package com.hitachi.drone.model.dto;

import lombok.Builder;

@Builder
public record MedicationResponse(
        String name,
        Double weight,
        String code,
        String image) {
}
