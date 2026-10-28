package com.mhbas.controller;

import com.mhbas.annotation.OperationLog;
import com.mhbas.context.BaseContext;
import com.mhbas.result.Result;
import com.mhbas.dto.GoalDTO;
import com.mhbas.vo.GoalVO;
import com.mhbas.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public Result<List<GoalVO.Detail>> list() {
        return Result.success(goalService.listWithProgress(BaseContext.requireCurrentId()));
    }

    @GetMapping("/types")
    public Result<List<GoalVO.TypeOption>> types() {
        return Result.success(goalService.typeOptions());
    }

    @PostMapping
    @OperationLog("新增健康目标")
    public Result<GoalVO.Detail> add(@Valid @RequestBody GoalDTO.Create request) {
        return Result.success(goalService.add(BaseContext.requireCurrentId(), request));
    }

    @PutMapping("/{id}/toggle")
    @OperationLog("切换目标状态")
    public Result<Void> toggle(@PathVariable Long id) {
        goalService.toggle(BaseContext.requireCurrentId(), id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog("删除健康目标")
    public Result<Void> delete(@PathVariable Long id) {
        goalService.delete(BaseContext.requireCurrentId(), id);
        return Result.success();
    }
}
