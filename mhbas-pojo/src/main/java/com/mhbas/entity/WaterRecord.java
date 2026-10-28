package com.mhbas.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class WaterRecord {
    private Long id;
    private Long userId;
    private LocalDate recordDate;
    private LocalTime waterTime;
    private Integer amountMl;
}
