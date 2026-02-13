package com.hitachi.drone.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record LoadMedicationRequest(
        @NotEmpty @Valid
        List<MedicationRequest> medications
) {
}
