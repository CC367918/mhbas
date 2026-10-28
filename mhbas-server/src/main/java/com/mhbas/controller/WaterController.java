package com.mhbas.controller;

import com.mhbas.annotation.OperationLog;
import com.mhbas.context.BaseContext;
import com.mhbas.result.Result;
import com.mhbas.dto.RecordDTO;
import com.mhbas.vo.RecordVO;
import com.mhbas.service.WaterService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/water")
public class WaterController {
    private final WaterService waterService;

    public WaterController(WaterService waterService) {
        this.waterService = waterService;
    }

    @GetMapping
    public Result<RecordVO.WaterOverview> overview() {
        return Result.success(waterService.overview(BaseContext.requireCurrentId()));
    }

    @PostMapping
    @OperationLog("新增饮水记录")
    public Result<Void> save(@Valid @RequestBody RecordDTO.Water request) {
        waterService.save(BaseContext.requireCurrentId(), request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog("删除饮水记录")
    public Result<Void> delete(@PathVariable Long id) {
        waterService.delete(BaseContext.requireCurrentId(), id);
        return Result.success();
    }
}
