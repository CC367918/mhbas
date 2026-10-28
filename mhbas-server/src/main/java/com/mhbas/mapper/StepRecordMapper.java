package com.mhbas.mapper;

import com.mhbas.entity.StepRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StepRecordMapper {
    StepRecord findByUserAndDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);

    List<StepRecord> findByUserIdBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    int insert(StepRecord record);

    int updateSteps(@Param("id") Long id, @Param("userId") Long userId, @Param("steps") Integer steps);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
