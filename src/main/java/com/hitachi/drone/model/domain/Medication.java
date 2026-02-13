package com.hitachi.drone.model.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@ToString
@Builder
public class Medication {
    String name;
    Double weight;
    String code;
    String image;
}
