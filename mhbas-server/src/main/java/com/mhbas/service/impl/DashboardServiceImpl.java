package com.mhbas.service.impl;

import com.mhbas.constant.HealthConstant;
import com.mhbas.service.AnalysisService;
import com.mhbas.service.DashboardService;
import com.mhbas.service.GoalService;
import com.mhbas.service.StepService;
import com.mhbas.vo.DashboardVO;
import com.mhbas.vo.GoalVO;
import com.mhbas.entity.FitnessRecord;
import com.mhbas.entity.MealRecord;
import com.mhbas.entity.SleepRecord;
import com.mhbas.entity.StepRecord;
import com.mhbas.entity.WaterRecord;
import com.mhbas.mapper.FitnessRecordMapper;
import com.mhbas.mapper.MealRecordMapper;
import com.mhbas.mapper.SleepRecordMapper;
import com.mhbas.mapper.StepRecordMapper;
import com.mhbas.mapper.WaterRecordMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final SleepRecordMapper sleepRecordMapper;
    private final FitnessRecordMapper fitnessRecordMapper;
    private final StepRecordMapper stepRecordMapper;
    private final WaterRecordMapper waterRecordMapper;
    private final MealRecordMapper mealRecordMapper;
    private final GoalService goalService;
    private final StepService stepService;
    private final AnalysisService analysisService;

    public DashboardServiceImpl(SleepRecordMapper sleepRecordMapper,
                            FitnessRecordMapper fitnessRecordMapper,
                            StepRecordMapper stepRecordMapper,
                            WaterRecordMapper waterRecordMapper,
                            MealRecordMapper mealRecordMapper,
                            GoalService goalService,
                            StepService stepService,
                            AnalysisService analysisService) {
        this.sleepRecordMapper = sleepRecordMapper;
        this.fitnessRecordMapper = fitnessRecordMapper;
        this.stepRecordMapper = stepRecordMapper;
        this.waterRecordMapper = waterRecordMapper;
        this.mealRecordMapper = mealRecordMapper;
        this.goalService = goalService;
        this.stepService = stepService;
        this.analysisService = analysisService;
    }

    public DashboardVO.Overview dashboard(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(6);

        List<SleepRecord> sleepRecords = sleepRecordMapper.findByUserAndRecordDateBetween(userId, from, today);
        List<FitnessRecord> fitnessRecords = fitnessRecordMapper.findByUserAndStartTimeBetween(
                userId, from.atStartOfDay(), today.plusDays(1).atStartOfDay()
        );
        List<StepRecord> stepRecords = stepRecordMapper.findByUserIdBetween(userId, from, today);
        List<WaterRecord> waterRecords = waterRecordMapper.findByUserAndRecordDateBetween(userId, from, today);
        List<MealRecord> mealRecords = mealRecordMapper.findByUserAndRecordDateBetween(userId, from, today);

        Map<LocalDate, Integer> sleepDaily = new HashMap<>();
        sleepRecords.forEach(record -> sleepDaily.merge(record.getRecordDate(),
                value(record.getDurationMinutes()), Integer::sum));
        Map<LocalDate, Integer> fitnessDaily = new HashMap<>();
        fitnessRecords.forEach(record -> fitnessDaily.merge(record.getStartTime().toLocalDate(),
                value(record.getDurationMinutes()), Integer::sum));
        Map<LocalDate, Integer> stepDaily = stepService.dailyMap(stepRecords);
        Map<LocalDate, Integer> waterDaily = new HashMap<>();
        waterRecords.forEach(record -> waterDaily.merge(record.getRecordDate(),
                value(record.getAmountMl()), Integer::sum));
        Map<LocalDate, Integer> mealDaily = new HashMap<>();
        mealRecords.forEach(record -> mealDaily.merge(record.getRecordDate(), 1, Integer::sum));

        List<String> labels = new ArrayList<>();
        List<Integer> sleepSeries = new ArrayList<>();
        List<Integer> fitnessSeries = new ArrayList<>();
        List<Integer> stepSeries = new ArrayList<>();
        List<Integer> waterSeries = new ArrayList<>();
        List<Integer> mealSeries = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.toString().substring(5));
            sleepSeries.add(sleepDaily.getOrDefault(date, 0));
            fitnessSeries.add(fitnessDaily.getOrDefault(date, 0));
            stepSeries.add(stepDaily.getOrDefault(date, 0));
            waterSeries.add(waterDaily.getOrDefault(date, 0));
            mealSeries.add(mealDaily.getOrDefault(date, 0));
        }

        List<FitnessRecord> todayFitness = fitnessRecords.stream()
                .filter(record -> record.getStartTime().toLocalDate().equals(today))
                .toList();
        int todayFitnessMinutes = todayFitness.stream()
                .mapToInt(record -> value(record.getDurationMinutes())).sum();
        int todayCalories = (int) Math.round(todayFitness.stream()
                .mapToDouble(record -> record.getCalories() == null ? 0 : record.getCalories()).sum());
        int todaySteps = stepDaily.getOrDefault(today, 0);
        Integer stepGoal = stepService.goalSteps(userId);

        DashboardVO.TodayStats todayStats = new DashboardVO.TodayStats(
                sleepDaily.getOrDefault(today, 0),
                todayFitness.size(),
                todayFitnessMinutes,
                todayCalories,
                todaySteps,
                stepGoal,
                waterDaily.getOrDefault(today, 0),
                mealDaily.getOrDefault(today, 0)
        );
        DashboardVO.Trend7 trend = new DashboardVO.Trend7(
                labels, sleepSeries, fitnessSeries, stepSeries, waterSeries, mealSeries
        );

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<FitnessRecord> weekFitness = fitnessRecordMapper.findByUserAndStartTimeBetween(
                userId, monday.atStartOfDay(), LocalDateTime.now().plusMinutes(1)
        );
        int weekFitnessMinutes = weekFitness.stream()
                .mapToInt(record -> value(record.getDurationMinutes())).sum();
        int weekWater = waterRecords.stream()
                .filter(record -> !record.getRecordDate().isBefore(monday))
                .mapToInt(record -> value(record.getAmountMl())).sum();
        double averageSleep = sleepRecords.stream()
                .mapToInt(record -> value(record.getDurationMinutes()))
                .average().orElse(0) / 60.0;

        DashboardVO.WeeklySummary weekly = new DashboardVO.WeeklySummary(
                weekFitness.size(),
                weekFitnessMinutes,
                Math.round(averageSleep * 10.0) / 10.0,
                stepService.weekSteps(userId),
                weekWater
        );
        List<GoalVO.Detail> goals = goalService.listWithProgress(userId).stream()
                .filter(GoalVO.Detail::enabled)
                .limit(4)
                .toList();
        return new DashboardVO.Overview(
                todayStats, trend, weekly, goals, analysisService.analyze(userId)
        );
    }

    private int value(Integer number) {
        return number == null ? 0 : number;
    }
}
