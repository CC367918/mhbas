package com.mhbas.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepRecord {
    private Long id;
    private Long userId;
    private LocalDate recordDate;
    private Integer steps;
    private LocalDateTime createdAt;

    public double getDistanceKm() {
        return steps == null ? 0 : Math.round(steps * 0.0007 * 10.0) / 10.0;
    }
}
