package com.hitachi.drone.model.entity;

import com.hitachi.drone.model.enums.DroneModel;
import com.hitachi.drone.model.enums.DroneState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "drones")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DroneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, unique = true, length = 100)
    String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DroneModel model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DroneState state;

    @Column(nullable = false)
    Double batteryCapacity;

    @Builder.Default
    @OneToMany(
            mappedBy = "drone",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<MedicationEntity> medications = new ArrayList<>();

    @Transient
    public Double getWeightLimit() {
        return model.getWeightLimit();
    }

    @Transient
    public Double getCurrentWeight() {
        return medications.stream()
                .mapToDouble(MedicationEntity::getWeight)
                .sum();
    }
}
