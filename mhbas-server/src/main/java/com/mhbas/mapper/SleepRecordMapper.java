package com.mhbas.mapper;

import com.github.pagehelper.Page;
import com.mhbas.entity.SleepRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SleepRecordMapper {
    List<SleepRecord> findByUserAndRecordDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    int insert(SleepRecord record);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    Page<SleepRecord> pageQuery(@Param("userId") Long userId,
                                @Param("begin") LocalDate begin,
                                @Param("end") LocalDate end);
}
