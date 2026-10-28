package com.mhbas.service;

import com.mhbas.vo.DashboardVO;

public interface DashboardService {
    DashboardVO.Overview dashboard(Long userId);
}
