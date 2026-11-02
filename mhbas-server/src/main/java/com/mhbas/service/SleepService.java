package com.mhbas.service;

import com.mhbas.dto.RecordDTO;
import com.mhbas.result.PageResult;
import com.mhbas.vo.RecordVO;

import java.time.LocalDate;

public interface SleepService {
    void save(Long userId, RecordDTO.Sleep sleepData);

    RecordVO.SleepOverview overview(Long userId);

    double averageHours7(Long userId);

    PageResult page(Long userId, int page, int pageSize, LocalDate begin, LocalDate end);

    void delete(Long userId, Long id);
}
