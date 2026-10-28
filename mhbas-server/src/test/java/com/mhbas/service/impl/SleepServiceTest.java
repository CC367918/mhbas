package com.mhbas.service.impl;

import com.mhbas.dto.RecordDTO;
import com.mhbas.entity.SleepRecord;
import com.mhbas.exception.BusinessException;
import com.mhbas.mapper.SleepRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SleepServiceTest {
    @Mock
    private SleepRecordMapper sleepRecordMapper;

    @InjectMocks
    private SleepServiceImpl sleepService;

    @Test
    void shouldRejectOverlappingSleepIntervals() {
        LocalDate date = LocalDate.now().minusDays(2);
        SleepRecord existing = sleepRecord(date, LocalTime.of(23, 0), LocalTime.of(7, 0), 480);
        when(sleepRecordMapper.findByUserAndRecordDateBetween(
                1L, date.minusDays(1), date.plusDays(1)))
                .thenReturn(List.of(existing));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                sleepService.save(1L, new RecordDTO.Sleep(
                        date,
                        LocalTime.of(22, 30),
                        LocalTime.of(6, 30),
                        "一般"
                ))
        );

        assertEquals(409, exception.getCode());
        verify(sleepRecordMapper, never()).insert(any());
    }

    private SleepRecord sleepRecord(LocalDate date, LocalTime sleep, LocalTime wake, int duration) {
        SleepRecord record = new SleepRecord();
        record.setRecordDate(date);
        record.setSleepTime(sleep);
        record.setWakeTime(wake);
        record.setDurationMinutes(duration);
        return record;
    }
}
