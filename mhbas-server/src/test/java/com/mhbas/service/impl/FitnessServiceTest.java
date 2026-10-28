package com.mhbas.service.impl;

import com.mhbas.vo.RecordVO;
import com.mhbas.entity.FitnessRecord;
import com.mhbas.mapper.FitnessRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FitnessServiceTest {
    @Mock
    private FitnessRecordMapper fitnessRecordMapper;

    @InjectMocks
    private FitnessServiceImpl fitnessService;

    @Test
    void shouldAggregateThisWeekTraining() {
        LocalDate today = LocalDate.now();
        LocalDateTime morning = today.atTime(8, 0);
        when(fitnessRecordMapper.findByUserAndStartTimeBetween(
                eq(1L), eq(today.minusDays(29).atStartOfDay()), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        fitness(morning, 45, 220.0),
                        fitness(morning.plusHours(9), 30, 130.0)
                ));

        RecordVO.FitnessOverview overview = fitnessService.overview(1L);

        assertEquals(2, overview.stats().weekCount());
        assertEquals(75, overview.stats().weekMinutes());
        assertEquals(350, overview.stats().weekCalories());
        assertEquals(75, overview.stats().maxTrend());
    }

    private FitnessRecord fitness(LocalDateTime startTime, int minutes, double calories) {
        FitnessRecord record = new FitnessRecord();
        record.setStartTime(startTime);
        record.setDurationMinutes(minutes);
        record.setCalories(calories);
        return record;
    }
}
