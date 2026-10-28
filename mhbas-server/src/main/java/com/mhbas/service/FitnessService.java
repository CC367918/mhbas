package com.mhbas.service;

import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;

public interface FitnessService {
    void save(Long userId, RecordDTO.Fitness request);

    RecordVO.FitnessOverview overview(Long userId);

    int todayCount(Long userId);

    void delete(Long userId, Long id);
}
