package com.mhbas.service;

import com.mhbas.dto.RecordDTO;
import com.mhbas.result.PageResult;
import com.mhbas.vo.RecordVO;

import java.time.LocalDate;

public interface FitnessService {
    void save(Long userId, RecordDTO.Fitness request);

    RecordVO.FitnessOverview overview(Long userId);

    int todayCount(Long userId);

    PageResult page(Long userId, int page, int pageSize, LocalDate begin, LocalDate end);

    void delete(Long userId, Long id);
}
