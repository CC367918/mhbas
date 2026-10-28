package com.mhbas.service.impl;

import com.mhbas.service.FitnessService;

import com.mhbas.exception.BusinessException;
import com.mhbas.constant.HealthConstant;
import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;
import com.mhbas.entity.FitnessRecord;
import com.mhbas.mapper.FitnessRecordMapper;
import com.mhbas.utils.FitnessCalorieCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FitnessServiceImpl implements FitnessService {
    private static final Set<String> ALLOWED_TYPES = Set.copyOf(HealthConstant.FITNESS_TYPES);
    private static final Set<String> ALLOWED_INTENSITIES =
            Set.copyOf(HealthConstant.FITNESS_INTENSITIES);

    private final FitnessRecordMapper fitnessRecordMapper;

    public FitnessServiceImpl(FitnessRecordMapper fitnessRecordMapper) {
        this.fitnessRecordMapper = fitnessRecordMapper;
    }

    @Transactional
    public void save(Long userId, RecordDTO.Fitness request) {
        if (!ALLOWED_TYPES.contains(request.fitnessType())) {
            throw new BusinessException("请选择有效的健身项目");
        }
        if (request.startTime().isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new BusinessException("训练开始时间不能晚于当前时间");
        }
        String intensity = StringUtils.hasText(request.intensity())
                ? request.intensity() : "中等强度";
        if (!ALLOWED_INTENSITIES.contains(intensity)) {
            throw new BusinessException("请选择有效的训练强度");
        }

        FitnessRecord record = new FitnessRecord();
        record.setUserId(userId);
        record.setFitnessType(request.fitnessType());
        record.setStartTime(request.startTime());
        record.setDurationMinutes(request.durationMinutes());
        record.setIntensity(intensity);
        record.setCalories(FitnessCalorieCalculator.estimate(
                request.fitnessType(), intensity, request.durationMinutes()
        ));
        record.setNote(StringUtils.hasText(request.note()) ? request.note().trim() : null);
        fitnessRecordMapper.insert(record);
    }

    public RecordVO.FitnessOverview overview(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(29);
        List<FitnessRecord> records = fitnessRecordMapper.findByUserAndStartTimeBetween(
                userId, from.atStartOfDay(), today.plusDays(1).atStartOfDay()
        );
        Map<LocalDate, Integer> daily = new HashMap<>();
        for (FitnessRecord record : records) {
            daily.merge(record.getStartTime().toLocalDate(),
                    record.getDurationMinutes() == null ? 0 : record.getDurationMinutes(), Integer::sum);
        }

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.toString().substring(5));
            values.add(daily.getOrDefault(date, 0));
        }

        LocalDateTime weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        List<FitnessRecord> weekRecords = records.stream()
                .filter(record -> !record.getStartTime().isBefore(weekStart))
                .toList();
        int weekMinutes = weekRecords.stream()
                .mapToInt(record -> record.getDurationMinutes() == null ? 0 : record.getDurationMinutes())
                .sum();
        int weekCalories = (int) Math.round(weekRecords.stream()
                .mapToDouble(record -> record.getCalories() == null ? 0 : record.getCalories())
                .sum());

        RecordVO.FitnessStats stats = new RecordVO.FitnessStats(
                weekRecords.size(),
                weekMinutes,
                weekCalories,
                labels,
                values,
                values.stream().mapToInt(Integer::intValue).max().orElse(0)
        );
        return new RecordVO.FitnessOverview(records, stats);
    }

    public int todayCount(Long userId) {
        LocalDate today = LocalDate.now();
        return fitnessRecordMapper.findByUserAndStartTimeBetween(
                userId, today.atStartOfDay(), today.plusDays(1).atStartOfDay()
        ).size();
    }

    public void delete(Long userId, Long id) {
        if (fitnessRecordMapper.deleteByIdAndUser(id, userId) == 0) {
            throw new BusinessException(404, "健身记录不存在");
        }
    }
}
