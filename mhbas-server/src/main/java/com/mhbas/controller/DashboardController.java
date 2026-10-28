package com.mhbas.controller;

import com.mhbas.context.BaseContext;
import com.mhbas.result.Result;
import com.mhbas.vo.DashboardVO;
import com.mhbas.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public Result<DashboardVO.Overview> dashboard() {
        return Result.success(dashboardService.dashboard(BaseContext.requireCurrentId()));
    }
}
