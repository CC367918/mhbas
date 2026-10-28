package com.mhbas.service.impl;

import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;
import com.mhbas.entity.Goal;
import com.mhbas.entity.StepRecord;
import com.mhbas.mapper.GoalMapper;
import com.mhbas.mapper.StepRecordMapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StepServiceTest {
    @Mock
    private StepRecordMapper stepRecordMapper;
    @Mock
    private GoalMapper goalMapper;

    @InjectMocks
    private StepServiceImpl stepService;

    @Test
    void shouldCalculateDailyGoalAndTrendStatistics() {
        LocalDate today = LocalDate.now();
        when(stepRecordMapper.findByUserIdBetween(eq(1L), eq(today.minusDays(29)), eq(today)))
                .thenReturn(List.of(
                        step(today, 9000),
                        step(today.minusDays(1), 7000),
                        step(today.minusDays(2), 8000)
                ));
        when(goalMapper.findActiveByUserAndType(1L, "STEPS_DAILY"))
                .thenReturn(stepGoal(8000));

        RecordVO.StepOverview overview = stepService.overview(1L);

        assertEquals(9000, overview.stats().todaySteps());
        assertEquals(3429, overview.stats().averageSteps7());
        assertEquals(2, overview.stats().goalDays7());
        assertEquals(6.3, overview.stats().distanceKm());
        assertEquals(8000, overview.stats().goalSteps());
        assertEquals(100, overview.stats().achievementRate());
        assertEquals(List.of(8000, 7000, 9000),
                overview.stats().trendValues().subList(11, 14));
    }

    @Test
    void shouldUseConfiguredStepGoal() {
        LocalDate today = LocalDate.now();
        when(stepRecordMapper.findByUserIdBetween(eq(1L), eq(today.minusDays(29)), eq(today)))
                .thenReturn(List.of(
                        step(today, 9000),
                        step(today.minusDays(1), 8500),
                        step(today.minusDays(2), 7000)
                ));
        when(goalMapper.findActiveByUserAndType(1L, "STEPS_DAILY"))
                .thenReturn(stepGoal(8500));

        RecordVO.StepStats stats = stepService.overview(1L).stats();

        assertEquals(8500, stats.goalSteps());
        assertEquals(2, stats.goalDays7());
        assertEquals(100, stats.achievementRate());
    }

    @Test
    void shouldReturnNoGoalMetricsWhenStepGoalIsMissing() {
        LocalDate today = LocalDate.now();
        when(stepRecordMapper.findByUserIdBetween(eq(1L), eq(today.minusDays(29)), eq(today)))
                .thenReturn(List.of(step(today, 6000)));
        when(goalMapper.findActiveByUserAndType(1L, "STEPS_DAILY")).thenReturn(null);

        RecordVO.StepStats stats = stepService.overview(1L).stats();

        assertNull(stats.goalSteps());
        assertNull(stats.goalDays7());
        assertNull(stats.achievementRate());
    }

    @Test
    void shouldUpdateExistingDailyRecordInsteadOfInsertingDuplicate() {
        LocalDate today = LocalDate.now();
        StepRecord existing = step(today, 5000);
        existing.setId(8L);
        when(stepRecordMapper.findByUserAndDate(1L, today)).thenReturn(existing);

        stepService.save(1L, new RecordDTO.Step(today, 8600));

        verify(stepRecordMapper).updateSteps(8L, 1L, 8600);
        verify(stepRecordMapper, never()).insert(any());
    }

    private StepRecord step(LocalDate date, int steps) {
        StepRecord record = new StepRecord();
        record.setRecordDate(date);
        record.setSteps(steps);
        return record;
    }

    private Goal stepGoal(double target) {
        Goal goal = new Goal();
        goal.setGoalType("STEPS_DAILY");
        goal.setTargetValue(target);
        return goal;
    }
}
