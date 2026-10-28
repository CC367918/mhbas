package com.mhbas.service;

import com.mhbas.dto.GoalDTO;
import com.mhbas.vo.GoalVO;

import java.util.List;

public interface GoalService {
    List<GoalVO.TypeOption> typeOptions();

    GoalVO.Detail add(Long userId, GoalDTO.Create request);

    List<GoalVO.Detail> listWithProgress(Long userId);

    void toggle(Long userId, Long id);

    void delete(Long userId, Long id);
}
