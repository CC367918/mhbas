package com.mhbas.service.impl;

import com.mhbas.service.StepService;

import com.mhbas.exception.BusinessException;
import com.mhbas.dto.RecordDTO;
import com.mhbas.entity.Goal;
import com.mhbas.vo.RecordVO;
import com.mhbas.entity.StepRecord;
import com.mhbas.enumeration.GoalType;
import com.mhbas.mapper.GoalMapper;
import com.mhbas.mapper.StepRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StepServiceImpl implements StepService {
    private final StepRecordMapper stepRecordMapper;
    private final GoalMapper goalMapper;

    public StepServiceImpl(StepRecordMapper stepRecordMapper, GoalMapper goalMapper) {
        this.stepRecordMapper = stepRecordMapper;
        this.goalMapper = goalMapper;
    }

    public RecordVO.StepOverview overview(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(29);
        List<StepRecord> records = stepRecordMapper.findByUserIdBetween(userId, from, today);
        Map<LocalDate, Integer> daily = dailyMap(records);

        List<String> trendLabels = new ArrayList<>();
        List<Integer> trendValues = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            trendLabels.add(date.toString().substring(5));
            trendValues.add(daily.getOrDefault(date, 0));
        }

        /*LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int weekSteps = 0;
        for (LocalDate date = monday; !date.isAfter(today); date = date.plusDays(1)) {
            weekSteps += daily.getOrDefault(date, 0);
        }*/

        Integer goalSteps = goalSteps(userId);
        int sum7 = 0;
        Integer goalDays7 = goalSteps == null ? null : 0;
        for (int i = 0; i < 7; i++) {
            int value = daily.getOrDefault(today.minusDays(i), 0);
            sum7 += value;
            if (goalSteps != null && value >= goalSteps) {
                goalDays7++;
            }
        }

        int todaySteps = daily.getOrDefault(today, 0);
        Integer achievementRate = goalSteps == null
                ? null
                : Math.min(100, (int) Math.round(todaySteps * 100.0 / goalSteps));
        RecordVO.StepStats stats = new RecordVO.StepStats(
                todaySteps,
                weekSteps(userId),
                (int) Math.round(sum7 / 7.0),
                goalDays7,
                Math.round(todaySteps * 0.0007 * 10.0) / 10.0,
                goalSteps,
                achievementRate,
                trendLabels,
                trendValues
        );
        return new RecordVO.StepOverview(records, stats);
    }

    public Map<LocalDate, Integer> dailyMap(List<StepRecord> records) {
        Map<LocalDate, Integer> daily = new HashMap<>();
        for (StepRecord record : records) {
            daily.put(record.getRecordDate(), record.getSteps() == null ? 0 : record.getSteps());
        }
        return daily;
    }

    public int todaySteps(Long userId) {
        StepRecord record = stepRecordMapper.findByUserAndDate(userId, LocalDate.now());
        return record == null || record.getSteps() == null ? 0 : record.getSteps();
    }

    public int weekSteps(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return stepRecordMapper.findByUserIdBetween(userId, monday, today).stream()
                .mapToInt(record -> record.getSteps() == null ? 0 : record.getSteps())
                .sum();
    }

    public Integer goalSteps(Long userId) {
        Goal goal = goalMapper.findActiveByUserAndType(userId, GoalType.STEPS_DAILY.getCode());
        if (goal == null || goal.getTargetValue() == null || goal.getTargetValue() <= 0) {
            return null;
        }
        return Math.max(1, (int) Math.round(goal.getTargetValue()));
    }

    @Transactional
    public void save(Long userId, RecordDTO.Step request) {
        if (request.recordDate().isAfter(LocalDate.now())) {
            throw new BusinessException("不能记录未来日期的步数");
        }
        StepRecord stepRecord = stepRecordMapper.findByUserAndDate(userId, request.recordDate());
        if (stepRecord == null) {
            StepRecord record=StepRecord.builder()
                    .userId(userId)
                    .recordDate(request.recordDate())
                    .steps(request.steps())
                    .build();
            stepRecordMapper.insert(record);
        } else {
            stepRecordMapper.updateSteps(stepRecord.getId(), userId, request.steps());
        }
    }

    public void delete(Long userId, Long id) {
        if (stepRecordMapper.deleteByIdAndUser(id, userId) == 0) {
            throw new BusinessException(404, "步数记录不存在");
        }
    }

}
