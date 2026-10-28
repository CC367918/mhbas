package com.mhbas.service;

import com.mhbas.vo.AnalysisVO;

public interface AnalysisService {
    AnalysisVO.Overview analyze(Long userId);
}
