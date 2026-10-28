package com.mhbas.controller;

import com.mhbas.annotation.OperationLog;
import com.mhbas.context.BaseContext;
import com.mhbas.result.Result;
import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;
import com.mhbas.service.SleepService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sleep")
public class SleepController {
    private final SleepService sleepService;

    public SleepController(SleepService sleepService) {
        this.sleepService = sleepService;
    }

    @GetMapping
    public Result<RecordVO.SleepOverview> overview() {
        return Result.success(sleepService.overview(BaseContext.requireCurrentId()));
    }

    @PostMapping
    @OperationLog("新增睡眠记录")
    public Result<Void> save(@Valid @RequestBody RecordDTO.Sleep sleepData) {
        sleepService.save(BaseContext.requireCurrentId(), sleepData);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog("删除睡眠记录")
    public Result<Void> delete(@PathVariable Long id) {
        sleepService.delete(BaseContext.requireCurrentId(), id);
        return Result.success();
    }
}
