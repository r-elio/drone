package com.hitachi.drone.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

@Builder
public record MedicationRequest(
        @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
        String name,
        @NotNull @Positive
        Double weight,
        @NotBlank @Pattern(regexp = "^[A-Z0-9_]+$")
        String code,
        @NotBlank @URL
        String image
) {
}
