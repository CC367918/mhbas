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
public class MealRecord {
    private Long id;
    private Long userId;
    private LocalDate recordDate;
    private String mealType;
    private LocalDateTime createdAt;
}
