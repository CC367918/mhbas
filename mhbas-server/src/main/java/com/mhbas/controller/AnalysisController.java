package com.mhbas.controller;

import com.mhbas.context.BaseContext;
import com.mhbas.result.Result;
import com.mhbas.vo.AnalysisVO;
import com.mhbas.service.AnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping
    public Result<AnalysisVO.Overview> analyze() {
        return Result.success(analysisService.analyze(BaseContext.requireCurrentId()));
    }
}
