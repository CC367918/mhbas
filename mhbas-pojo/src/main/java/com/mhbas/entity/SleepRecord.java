package com.mhbas.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleepRecord {
    private Long id;
    private Long userId;    // 用户ID
    private LocalDate recordDate;    // 记录日期
    private LocalTime sleepTime;    // 睡眠时间
    private LocalTime wakeTime;    // 唤醒时间
    private Integer durationMinutes;    // 持续分钟数
    private String subjectiveStatus;    // 主观状态

    public String getDurationText() {
        if (durationMinutes == null) {
            return "-";
        }
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        return minutes == 0 ? hours + " 小时" : hours + " 小时 " + minutes + " 分";
    }
}
