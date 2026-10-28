package com.mhbas.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FitnessRecord {
    private Long id;
    private Long userId;
    private String fitnessType;
    private LocalDateTime startTime;
    private Integer durationMinutes;
    private Double calories;
    private String intensity;
    private String note;
}
