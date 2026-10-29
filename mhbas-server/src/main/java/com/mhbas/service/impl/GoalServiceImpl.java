package com.mhbas.service.impl;

import com.mhbas.service.GoalService;
import com.mhbas.service.FitnessService;
import com.mhbas.service.MealService;
import com.mhbas.service.SleepService;
import com.mhbas.service.StepService;
import com.mhbas.service.WaterService;
import com.mhbas.exception.BusinessException;
import com.mhbas.dto.GoalDTO;
import com.mhbas.vo.GoalVO;
import com.mhbas.vo.RecordVO;
import com.mhbas.entity.Goal;
import com.mhbas.enumeration.GoalType;
import com.mhbas.mapper.GoalMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalMapper goalMapper;
    private final FitnessService fitnessService;
    private final SleepService sleepService;
    private final WaterService waterService;
    private final MealService mealService;
    private final StepService stepService;

    public GoalServiceImpl(GoalMapper goalMapper, FitnessService fitnessService, SleepService sleepService, WaterService waterService, MealService mealService, StepService stepService) {
        this.goalMapper = goalMapper;
        this.fitnessService = fitnessService;
        this.sleepService = sleepService;
        this.waterService = waterService;
        this.mealService = mealService;
        this.stepService = stepService;
    }

    private record CurrentValues(RecordVO.FitnessStats fitness, double sleepHours, int waterMl, int mealCount, int steps) {}

    /**
     * 获取用户所有目标的详情，包含进度
     * @param userId
     * @return
     */
    public List<GoalVO.Detail> listWithProgress(Long userId) {
        List<Goal> goals = goalMapper.findByUser(userId);
        CurrentValues current = currentValues(userId);
        return goals.stream().map(goal -> toView(goal, current)).toList();
    }

    /**
     * 获取目标类型选项
     * @return
     */
    public List<GoalVO.TypeOption> typeOptions() {
        return Arrays.stream(GoalType.values())
                .map(type -> new GoalVO.TypeOption(
                        type.getCode(),
                        type.getDisplayName(),
                        type.getUnit(),
                        type.getPeriod(),
                        type.getSuggestedTarget()
                ))
                .toList();
    }
    /**
     * 添加目标
     * @param userId
     * @param request
     * @return
     */

    @Transactional
    public GoalVO.Detail add(Long userId, GoalDTO.Create request) {
        GoalType meta = GoalType.fromCode(request.goalType())
                .orElseThrow(() -> new BusinessException("不支持的目标类型"));
        Goal duplicate = goalMapper.findDuplicate(
                userId, meta.getCode(), request.targetValue(), meta.getPeriod()
        );
        if (duplicate != null) {
            throw new BusinessException(409, "相同的健康目标已存在，请直接启用或修改原目标");
        }
        Goal goal = Goal.builder()
                .userId(userId)
                .goalType(meta.getCode())
                .goalName(meta.getDisplayName())
                .targetValue(request.targetValue())
                .unit(meta.getUnit())
                .period(meta.getPeriod())
                .enabled(true)
                .build();
        try {
            goalMapper.insert(goal);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(409, "相同的健康目标已存在，请直接启用或修改原目标");
        }
        return toView(goalMapper.findByIdAndUser(goal.getId(), userId), currentValues(userId));
    }
    /**
     * 切换目标状态
     * @param userId
     * @param id
     */

    @Transactional
    public void toggle(Long userId, Long id) {
        Goal goal = goalMapper.findByIdAndUser(id, userId);
        if (goal == null) {
            throw new BusinessException(404, "目标不存在");
        }
        goalMapper.updateEnabled(id, userId, !Boolean.TRUE.equals(goal.getEnabled()));
    }
    /**
     * 删除目标
     * @param userId
     * @param id
     */
    public void delete(Long userId, Long id) {
        if (goalMapper.deleteByIdAndUser(id, userId) == 0) {
            throw new BusinessException(404, "目标不存在");
        }
    }
    //将实体类转换为视图对象
    private GoalVO.Detail toView(Goal goal, CurrentValues current) {
        if (goal == null) {
            throw new BusinessException(404, "目标不存在");
        }
        double value = switch (goal.getGoalType()) {
            case "FITNESS_TIMES" -> current.fitness().weekCount();
            case "FITNESS_MINUTES" -> current.fitness().weekMinutes();
            case "SLEEP_HOURS" -> current.sleepHours();
            case "WATER_ML" -> current.waterMl();
            case "MEAL_COUNT" -> current.mealCount();
            case "STEPS_DAILY" -> current.steps();
            default -> 0;
        };
        double target = goal.getTargetValue() == null ? 0 : goal.getTargetValue();
        int progress = target <= 0 ? 0 : Math.min(100, (int) Math.round(value / target * 100));
        return new GoalVO.Detail(
                goal.getId(),
                goal.getGoalType(),
                goal.getGoalName(),
                goal.getTargetValue(),
                goal.getUnit(),
                goal.getPeriod(),
                goal.getEnabled(),
                Math.round(value * 10.0) / 10.0,
                progress,
                value >= target
        );
    }

    private CurrentValues currentValues(Long userId) {
        RecordVO.FitnessStats fitness = fitnessService.overview(userId).stats();
        return new CurrentValues(
                fitness,
                sleepService.averageHours7(userId),
                waterService.todayMl(userId),
                mealService.todayCount(userId),
                stepService.todaySteps(userId)
        );
    }
}
