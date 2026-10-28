package com.mhbas.mapper;

import com.mhbas.entity.MealRecord;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MealRecordMapper {
    List<MealRecord> findByUserAndRecordDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    int insert(MealRecord record);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    MealRecord getMeal(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate, @Param("mealType") String mealType);
}
