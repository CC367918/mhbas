package com.mhbas.controller;

import com.mhbas.context.BaseContext;
import com.mhbas.result.Result;
import com.mhbas.service.ReportService;
import com.mhbas.vo.DashboardVO;
import com.mhbas.service.DashboardService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    private final ReportService reportService;

    public DashboardController(DashboardService dashboardService, ReportService reportService) {
        this.reportService = reportService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public Result<DashboardVO.Overview> dashboard() {
        return Result.success(dashboardService.dashboard(BaseContext.requireCurrentId()));
    }

    @GetMapping("/weekly-report")
    public void exportWeeklyReport(HttpServletResponse response) throws IOException {
        reportService.exportWeeklyReport(BaseContext.requireCurrentId(),response);
    }
}
