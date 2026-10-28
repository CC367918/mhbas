package com.mhbas.mapper;

import com.mhbas.entity.Goal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoalMapper {
    List<Goal> findByUser(@Param("userId") Long userId);

    Goal findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    Goal findActiveByUserAndType(@Param("userId") Long userId,
                                 @Param("goalType") String goalType);

    Goal findDuplicate(@Param("userId") Long userId, @Param("goalType") String goalType, @Param("targetValue") Double targetValue, @Param("period") String period);

    int insert(Goal goal);

    int updateEnabled(@Param("id") Long id, @Param("userId") Long userId, @Param("enabled") boolean enabled);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
