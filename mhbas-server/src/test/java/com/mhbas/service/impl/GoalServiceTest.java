package com.mhbas.service.impl;

import com.mhbas.dto.GoalDTO;
import com.mhbas.entity.Goal;
import com.mhbas.exception.BusinessException;
import com.mhbas.mapper.GoalMapper;
import com.mhbas.service.FitnessService;
import com.mhbas.service.MealService;
import com.mhbas.service.SleepService;
import com.mhbas.service.StepService;
import com.mhbas.service.WaterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {
    @Mock
    private GoalMapper goalMapper;
    @Mock
    private FitnessService fitnessService;
    @Mock
    private SleepService sleepService;
    @Mock
    private WaterService waterService;
    @Mock
    private MealService mealService;
    @Mock
    private StepService stepService;

    @Test
    void shouldRejectFullyDuplicateGoal() {
        GoalServiceImpl goalService = new GoalServiceImpl(
                goalMapper, fitnessService, sleepService, waterService, mealService, stepService
        );
        when(goalMapper.findDuplicate(1L, "STEPS_DAILY", 8000.0, "DAY"))
                .thenReturn(new Goal());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                goalService.add(1L, new GoalDTO.Create("STEPS_DAILY", 8000.0))
        );

        assertEquals(409, exception.getCode());
        verify(goalMapper, never()).insert(any());
    }
}
