package com.mhbas.service;

import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;

public interface WaterService {
    void save(Long userId, RecordDTO.Water request);

    RecordVO.WaterOverview overview(Long userId);

    int todayMl(Long userId);

    int weekMl(Long userId);

    void delete(Long userId, Long id);
}
