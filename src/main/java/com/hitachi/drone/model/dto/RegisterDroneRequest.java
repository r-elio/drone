package com.hitachi.drone.model.dto;


import com.hitachi.drone.model.enums.DroneModel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterDroneRequest(
        @NotBlank @Size(max = 100)
        String serialNumber,
        @NotNull
        DroneModel model,
        @NotNull @Min(0) @Max(100)
        Double batteryCapacity
) {
}
