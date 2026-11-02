package com.mhbas.controller;

import com.mhbas.annotation.OperationLog;
import com.mhbas.context.BaseContext;
import com.mhbas.result.PageResult;
import com.mhbas.result.Result;
import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;
import com.mhbas.service.StepService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/steps")
public class StepController {
    private final StepService stepService;

    public StepController(StepService stepService) {
        this.stepService = stepService;
    }

    @GetMapping
    public Result<RecordVO.StepOverview> overview() {
        return Result.success(stepService.overview(BaseContext.requireCurrentId()));
    }

    @GetMapping("/page")
    public Result<PageResult> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) LocalDate begin,
            @RequestParam(required = false) LocalDate end) {
        return Result.success(stepService.page(BaseContext.requireCurrentId(), page, pageSize, begin, end));
    }

    @PostMapping
    @OperationLog("保存步数")
    public Result<Void> save(@Valid @RequestBody RecordDTO.Step request) {
        stepService.save(BaseContext.requireCurrentId(), request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog("删除步数记录")
    public Result<Void> delete(@PathVariable Long id) {
        stepService.delete(BaseContext.requireCurrentId(), id);
        return Result.success();
    }
}
