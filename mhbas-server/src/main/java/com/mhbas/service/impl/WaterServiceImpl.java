package com.mhbas.service.impl;

import com.mhbas.service.WaterService;

import com.mhbas.exception.BusinessException;
import com.mhbas.constant.HealthConstant;
import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;
import com.mhbas.entity.Goal;
import com.mhbas.entity.WaterRecord;
import com.mhbas.mapper.GoalMapper;
import com.mhbas.mapper.WaterRecordMapper;
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
public class WaterServiceImpl implements WaterService {
    private final WaterRecordMapper waterRecordMapper;
    private final GoalMapper goalMapper;

    public WaterServiceImpl(WaterRecordMapper waterRecordMapper, GoalMapper goalMapper) {
        this.waterRecordMapper = waterRecordMapper;
        this.goalMapper = goalMapper;
    }

    public RecordVO.WaterOverview overview(Long userId) {
        LocalDate today = LocalDate.now();
        List<WaterRecord> records = waterRecordMapper.findByUserAndRecordDateBetween
                (userId, today.minusDays(29), today);

        Map<LocalDate, Integer> daily = dailyMap(records);  //每日饮水总量
        int todayMl = daily.getOrDefault(today, 0);

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int weekMl = 0;
        for (LocalDate date = monday; !date.isAfter(today); date = date.plusDays(1)) {
            weekMl += daily.getOrDefault(date, 0);
        }

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.toString().substring(5));
            values.add(daily.getOrDefault(date, 0));
        }

        Goal activeGoal = goalMapper.findActiveByUserAndType(userId, "WATER_ML");
        Integer targetMl = activeGoal == null || activeGoal.getTargetValue() == null
                ? null : (int) Math.round(activeGoal.getTargetValue());
        Integer achievementRate = targetMl == null || targetMl <= 0
                ? null : Math.min(100, (int) Math.round(todayMl * 100.0 / targetMl));

        RecordVO.WaterStats stats = new RecordVO.WaterStats(
                todayMl, weekMl, targetMl, achievementRate, labels, values
        );
        return new RecordVO.WaterOverview(records, stats);
    }

    public int todayMl(Long userId) {
        LocalDate today = LocalDate.now();
        return waterRecordMapper.findByUserAndRecordDateBetween(userId, today, today).stream()
                .mapToInt(record -> record.getAmountMl() == null ? 0 : record.getAmountMl())
                .sum();
    }

    public int weekMl(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return waterRecordMapper.findByUserAndRecordDateBetween(userId, monday, today).stream()
                .mapToInt(record -> record.getAmountMl() == null ? 0 : record.getAmountMl())
                .sum();
    }
    //获得每日饮水总量 （时间，总饮水量）
    private Map<LocalDate, Integer> dailyMap(List<WaterRecord> records) {
        Map<LocalDate, Integer> daily = new HashMap<>();
        for (WaterRecord record : records) {
            daily.merge(record.getRecordDate(),
                    record.getAmountMl() == null ? 0 : record.getAmountMl(), Integer::sum);
        }
        return daily;
    }

    @Transactional
    public void save(Long userId, RecordDTO.Water request) {
        if (request.recordDate().isAfter(LocalDate.now())) {
            throw new BusinessException("不能记录未来日期的饮水");
        }
        WaterRecord record = new WaterRecord();
        record.setUserId(userId);
        record.setRecordDate(request.recordDate());
        record.setWaterTime(request.waterTime());
        record.setAmountMl(request.amountMl());
        waterRecordMapper.insert(record);
    }



    public void delete(Long userId, Long id) {
        if (waterRecordMapper.deleteByIdAndUser(id, userId) == 0) {
            throw new BusinessException(404, "饮水记录不存在");
        }
    }


}
