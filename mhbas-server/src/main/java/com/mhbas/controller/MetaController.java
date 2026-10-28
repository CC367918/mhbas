package com.mhbas.controller;

import com.mhbas.constant.HealthConstant;
import com.mhbas.context.BaseContext;
import com.mhbas.result.Result;
import com.mhbas.service.StepService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meta")
public class MetaController {
    private final StepService stepService;

    public MetaController(StepService stepService) {
        this.stepService = stepService;
    }

    @GetMapping
    public Result<Map<String, Object>> options() {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("fitnessTypes", HealthConstant.FITNESS_TYPES);
        options.put("intensities", HealthConstant.FITNESS_INTENSITIES);
        options.put("mealTypes", HealthConstant.MEAL_TYPES);
        options.put("sleepStatuses", HealthConstant.SLEEP_STATUSES);
        options.put("stepGoal", stepService.goalSteps(BaseContext.requireCurrentId()));
        return Result.success(options);
    }
}
