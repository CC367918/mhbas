package com.mhbas.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.mhbas.service.MealService;

import com.mhbas.exception.BusinessException;
import com.mhbas.constant.HealthConstant;
import com.mhbas.dto.RecordDTO;
import com.mhbas.result.PageResult;
import com.mhbas.vo.RecordVO;
import com.mhbas.entity.MealRecord;
import com.mhbas.mapper.MealRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MealServiceImpl implements MealService {
    private static final Set<String> ALLOWED_TYPES = Set.copyOf(HealthConstant.MEAL_TYPES);

    private final MealRecordMapper mealRecordMapper;

    public MealServiceImpl(MealRecordMapper mealRecordMapper) {
        this.mealRecordMapper = mealRecordMapper;
    }

    @Transactional
    public void save(Long userId, RecordDTO.Meal request) {
        if (!ALLOWED_TYPES.contains(request.mealType())) {
            throw new BusinessException("请选择有效的餐次");
        }
        if (request.recordDate().isAfter(LocalDate.now())) {
            throw new BusinessException("不能记录未来日期的三餐");
        }
        MealRecord meal=mealRecordMapper.getMeal(userId,request.recordDate(),request.mealType());
        if(meal!=null){
            throw new BusinessException("该餐次已经记录过，不可重复记录");
        }
        MealRecord record = MealRecord.builder()
                .userId(userId)
                .recordDate(request.recordDate())
                .mealType(request.mealType())
                .build();
        mealRecordMapper.insert(record);
    }

    public RecordVO.MealOverview overview(Long userId) {
        LocalDate today = LocalDate.now();
        List<MealRecord> records = mealRecordMapper.findByUserAndRecordDateBetween(userId, today.minusDays(29), today);
        Map<LocalDate, Integer> daily = dailyMap(records);

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int weekCount = 0;
        for (LocalDate date = monday; !date.isAfter(today); date = date.plusDays(1)) {
            weekCount += daily.getOrDefault(date, 0);
        }

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.toString().substring(5));
            values.add(daily.getOrDefault(date, 0));
        }

        return new RecordVO.MealOverview(
                daily.getOrDefault(today, 0), weekCount, labels, values
        );
    }

    public int todayCount(Long userId) {
        LocalDate today = LocalDate.now();
        return mealRecordMapper.findByUserAndRecordDateBetween(userId, today, today).size();
    }

    public Map<LocalDate, Integer> dailyMap(List<MealRecord> records) {
        Map<LocalDate, Integer> daily = new HashMap<>();
        for (MealRecord record : records) {
            daily.merge(record.getRecordDate(), 1, Integer::sum);
        }
        return daily;
    }

    @Override
    public PageResult page(Long userId, int page, int pageSize, LocalDate begin, LocalDate end) {
        int pageNum = Math.max(page, 1);
        int size = pageSize < 1 ? 10 : Math.min(pageSize, 100);
        if (begin != null && end != null && begin.isAfter(end)) {
            LocalDate temp = begin;
            begin = end;
            end = temp;
        }
        PageHelper.startPage(pageNum, size);
        Page<MealRecord> records = mealRecordMapper.pageQuery(userId, begin, end);
        return new PageResult(records.getTotal(), records.getResult());
    }

    public void delete(Long userId, Long id) {
        if (mealRecordMapper.deleteByIdAndUser(id, userId) == 0) {
            throw new BusinessException(404, "三餐记录不存在");
        }
    }
}
