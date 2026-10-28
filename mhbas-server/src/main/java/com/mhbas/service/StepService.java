package com.mhbas.service;

import com.mhbas.dto.RecordDTO;
import com.mhbas.entity.StepRecord;
import com.mhbas.vo.RecordVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StepService {
    void save(Long userId, RecordDTO.Step request);

    RecordVO.StepOverview overview(Long userId);

    int todaySteps(Long userId);

    int weekSteps(Long userId);

    Integer goalSteps(Long userId);

    void delete(Long userId, Long id);

    Map<LocalDate, Integer> dailyMap(List<StepRecord> records);
}
