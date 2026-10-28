package com.mhbas.service;

import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;

public interface SleepService {
    void save(Long userId, RecordDTO.Sleep sleepData);

    RecordVO.SleepOverview overview(Long userId);

    double averageHours7(Long userId);

    void delete(Long userId, Long id);
}
