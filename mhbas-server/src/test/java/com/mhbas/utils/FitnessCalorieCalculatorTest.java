package com.mhbas.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FitnessCalorieCalculatorTest {

    @Test
    void shouldEstimateCaloriesFromTypeDurationAndIntensity() {
        double medium = FitnessCalorieCalculator.estimate("力量训练", "中等强度", 45);
        double high = FitnessCalorieCalculator.estimate("力量训练", "高强度", 45);

        assertEquals(255.9, medium);
        assertEquals(294.3, high);
    }
}
