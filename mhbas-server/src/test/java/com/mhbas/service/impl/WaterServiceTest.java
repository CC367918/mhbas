package com.mhbas.service.impl;

import com.mhbas.entity.Goal;
import com.mhbas.entity.WaterRecord;
import com.mhbas.mapper.GoalMapper;
import com.mhbas.mapper.WaterRecordMapper;
import com.mhbas.vo.RecordVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaterServiceTest {
    @Mock
    private WaterRecordMapper waterRecordMapper;
    @Mock
    private GoalMapper goalMapper;

    @InjectMocks
    private WaterServiceImpl waterService;

    @Test
    void shouldUseActiveWaterGoalAsTarget() {
        LocalDate today = LocalDate.now();
        WaterRecord record = new WaterRecord();
        record.setRecordDate(today);
        record.setAmountMl(500);
        when(waterRecordMapper.findByUserAndRecordDateBetween(eq(1L), eq(today.minusDays(29)), eq(today)))
                .thenReturn(List.of(record));
        Goal goal = new Goal();
        goal.setGoalType("WATER_ML");
        goal.setTargetValue(2000.0);
        when(goalMapper.findActiveByUserAndType(1L, "WATER_ML")).thenReturn(goal);

        RecordVO.WaterStats stats = waterService.overview(1L).stats();

        assertEquals(2000, stats.targetMl());
        assertEquals(25, stats.achievementRate());
    }

    @Test
    void shouldReturnNoTargetWhenWaterGoalIsMissing() {
        LocalDate today = LocalDate.now();
        when(waterRecordMapper.findByUserAndRecordDateBetween(eq(1L), eq(today.minusDays(29)), eq(today)))
                .thenReturn(List.of());
        when(goalMapper.findActiveByUserAndType(1L, "WATER_ML")).thenReturn(null);

        RecordVO.WaterStats stats = waterService.overview(1L).stats();

        assertNull(stats.targetMl());
        assertNull(stats.achievementRate());
    }
}
