package com.mhbas.service.impl;

import com.mhbas.service.SleepService;

import com.mhbas.exception.BusinessException;
import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;
import com.mhbas.entity.SleepRecord;
import com.mhbas.mapper.SleepRecordMapper;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SleepServiceImpl implements SleepService {
    private final SleepRecordMapper sleepRecordMapper;

    public SleepServiceImpl(SleepRecordMapper sleepRecordMapper) {
        this.sleepRecordMapper = sleepRecordMapper;
    }

    @Transactional
    public void save(Long userId, RecordDTO.Sleep sleepData) {
        if (sleepData.recordDate().isAfter(LocalDate.now())) {
            throw new BusinessException("不能记录未来日期的睡眠");
        }
        if (sleepData.sleepTime().equals(sleepData.wakeTime())) {
            throw new BusinessException("入睡时间和起床时间不能完全相同");
        }
        LocalTime sleepTime=sleepData.sleepTime();
        LocalTime wakeTime=sleepData.wakeTime();
        int durationMinutes = calculateDuration(sleepData.sleepTime(), sleepData.wakeTime());

        List<SleepRecord> records = sleepRecordMapper.findByUserAndRecordDateBetween(
                userId, sleepData.recordDate().minusDays(1), sleepData.recordDate().plusDays(1)
        );
        boolean overlaps = false;
        for (SleepRecord record : records) {
            //判断区间是否有重叠
            if (sleepTime.isAfter(record.getSleepTime()) && wakeTime.isBefore(record.getWakeTime())) {
                overlaps = true;
                break;
            }
        }
        if (overlaps) {
            throw new BusinessException(409, "该睡眠时间与已有记录重叠，请检查入睡和起床时间");
        }

        SleepRecord record = SleepRecord.builder()
                .userId(userId)
                .recordDate(sleepData.recordDate())
                .sleepTime(sleepTime)
                .wakeTime(wakeTime)
                .durationMinutes(durationMinutes)
                .subjectiveStatus(StringUtils.hasText(sleepData.subjectiveStatus())
                        ? sleepData .subjectiveStatus() : "一般")
                .build();
        sleepRecordMapper.insert(record);
    }

    private int calculateDuration(LocalTime sleepTime, LocalTime wakeTime) {
        int seconds = wakeTime.toSecondOfDay() - sleepTime.toSecondOfDay();
        if (seconds <= 0) {
            seconds += 24 * 3600;
        }
        return Math.max(1, seconds / 60);
    }

    public RecordVO.SleepOverview overview(Long userId) {
        LocalDate today = LocalDate.now();
        //前30天记录
        List<SleepRecord> records = sleepRecordMapper.findByUserAndRecordDateBetween(userId, today.minusDays(29), today);
        Map<LocalDate, Integer> daily = new HashMap<>();
        for (SleepRecord record : records) {
            daily.merge(record.getRecordDate(),
                    record.getDurationMinutes() == null ? 0 : record.getDurationMinutes(), Integer::sum);
        }

        List<SleepRecord> recent7=sleepRecordMapper.findByUserAndRecordDateBetween(userId, today.minusDays(6), today);
        List<Integer> sleepTimeList=new ArrayList<>();
        for(SleepRecord record: recent7){
            LocalTime time=record.getSleepTime();
            int minutes = record.getSleepTime() == null ? 0 : time.getHour() * 60 + time.getMinute();
            sleepTimeList.add(minutes < 12 * 60 ? minutes + 24 * 60 : minutes);
        }
        double averageHours = averageHours7(userId);

        int fluctuation = recent7.isEmpty() ? 0 : standardDeviation(sleepTimeList);
        String regularity="暂无数据";
        if(!recent7.isEmpty()){
            if (fluctuation < 30) {
                regularity = "作息规律";
            }else if (fluctuation <= 60) {
                regularity = "作息一般";
            }else{
                regularity = "需要调整";
            }
        }
        //过去两周趋势标签
        List<String> trendLabels = new ArrayList<>(); // 趋势标签
        //过去两周趋势值
        List<Double> trendValues = new ArrayList<>(); // 趋势值
        for (int i = 13; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            trendLabels.add(date.toString().substring(5));
            trendValues.add(Precision.round(daily.getOrDefault(date, 0) / 60.0,1));
        }

        RecordVO.SleepStats stats = new RecordVO.SleepStats(
                Precision.round(averageHours, 1),
                fluctuation,
                regularity,
                recent7.size(),
                trendLabels,
                trendValues
        );
        return new RecordVO.SleepOverview(records, stats);
    }

    private int standardDeviation(List<Integer> values) {
        if (values.size() < 2) {
            return 0;
        }
        double[] samples = values.stream()
                .mapToDouble(Integer::doubleValue)
                .toArray();
        return (int) Math.round(new StandardDeviation(false).evaluate(samples));
    }


    //获得近七天平均睡眠 首页展示
    public double averageHours7(Long userId) {
        LocalDate today = LocalDate.now();
        return sleepRecordMapper.findByUserAndRecordDateBetween(userId, today.minusDays(6), today).stream()
                .mapToInt(record -> record.getDurationMinutes() == null ? 0 : record.getDurationMinutes())
                .average().orElse(0) / 60.0;
    }

    public void delete(Long userId, Long id) {
        if (sleepRecordMapper.deleteByIdAndUser(id, userId) == 0) {
            throw new BusinessException(404, "睡眠记录不存在");
        }
    }
}
