package com.mhbas.mapper;

import com.github.pagehelper.Page;
import com.mhbas.entity.FitnessRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FitnessRecordMapper {
    List<FitnessRecord> findByUserAndStartTimeBetween(@Param("userId") Long userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    int insert(FitnessRecord record);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    Page<FitnessRecord> pageQuery(@Param("userId") Long userId,
                                  @Param("begin") LocalDateTime begin,
                                  @Param("endExclusive") LocalDateTime endExclusive);
}
