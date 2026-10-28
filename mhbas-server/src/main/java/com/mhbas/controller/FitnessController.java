package com.mhbas.controller;

import com.mhbas.annotation.OperationLog;
import com.mhbas.context.BaseContext;
import com.mhbas.result.Result;
import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;
import com.mhbas.service.FitnessService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fitness")
public class FitnessController {
    private final FitnessService fitnessService;

    public FitnessController(FitnessService fitnessService) {
        this.fitnessService = fitnessService;
    }

    @GetMapping
    public Result<RecordVO.FitnessOverview> overview() {
        return Result.success(fitnessService.overview(BaseContext.requireCurrentId()));
    }

    @PostMapping
    @OperationLog("新增健身记录")
    public Result<Void> save(@Valid @RequestBody RecordDTO.Fitness request) {
        fitnessService.save(BaseContext.requireCurrentId(), request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog("删除健身记录")
    public Result<Void> delete(@PathVariable Long id) {
        fitnessService.delete(BaseContext.requireCurrentId(), id);
        return Result.success();
    }
}
