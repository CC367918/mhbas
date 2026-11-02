package com.mhbas.service;

import com.mhbas.dto.RecordDTO;
import com.mhbas.result.PageResult;
import com.mhbas.vo.RecordVO;

import java.time.LocalDate;

public interface WaterService {
    void save(Long userId, RecordDTO.Water request);

    RecordVO.WaterOverview overview(Long userId);

    int todayMl(Long userId);

    int weekMl(Long userId);

    PageResult page(Long userId, int page, int pageSize, LocalDate begin, LocalDate end);

    void delete(Long userId, Long id);
}
