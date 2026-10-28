package com.mhbas.service;

import com.mhbas.dto.RecordDTO;
import com.mhbas.entity.MealRecord;
import com.mhbas.vo.RecordVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MealService {
    void save(Long userId, RecordDTO.Meal request);

    RecordVO.MealOverview overview(Long userId);

    int todayCount(Long userId);

    Map<LocalDate, Integer> dailyMap(List<MealRecord> records);

    void delete(Long userId, Long id);
}
