package com.mhbas.service.impl;

import com.mhbas.service.AnalysisService;

import com.mhbas.vo.AnalysisVO;
import com.mhbas.entity.FitnessRecord;
import com.mhbas.entity.MealRecord;
import com.mhbas.entity.SleepRecord;
import com.mhbas.entity.StepRecord;
import com.mhbas.entity.WaterRecord;
import com.mhbas.mapper.FitnessRecordMapper;
import com.mhbas.mapper.MealRecordMapper;
import com.mhbas.mapper.SleepRecordMapper;
import com.mhbas.mapper.StepRecordMapper;
import com.mhbas.mapper.WaterRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisServiceImpl implements AnalysisService {
    private final SleepRecordMapper sleepRecordMapper;
    private final FitnessRecordMapper fitnessRecordMapper;
    private final StepRecordMapper stepRecordMapper;
    private final WaterRecordMapper waterRecordMapper;
    private final MealRecordMapper mealRecordMapper;

    public AnalysisServiceImpl(SleepRecordMapper sleepRecordMapper, FitnessRecordMapper fitnessRecordMapper, StepRecordMapper stepRecordMapper, WaterRecordMapper waterRecordMapper, MealRecordMapper mealRecordMapper) {
        this.sleepRecordMapper = sleepRecordMapper;
        this.fitnessRecordMapper = fitnessRecordMapper;
        this.stepRecordMapper = stepRecordMapper;
        this.waterRecordMapper = waterRecordMapper;
        this.mealRecordMapper = mealRecordMapper;
    }

    public AnalysisVO.Overview analyze(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(13);

        Map<LocalDate, Integer> sleep = new HashMap<>();
        sleepRecordMapper.findByUserAndRecordDateBetween(userId, from, today)
                .forEach(record -> sleep.merge(record.getRecordDate(), value(record.getDurationMinutes()), Integer::sum));

        Map<LocalDate, Integer> fitness = new HashMap<>();
        fitnessRecordMapper.findByUserAndStartTimeBetween(
                        userId, from.atStartOfDay(), today.plusDays(1).atStartOfDay())
                .forEach(record -> fitness.merge(record.getStartTime().toLocalDate(),
                        value(record.getDurationMinutes()), Integer::sum));

        Map<LocalDate, Integer> steps = new HashMap<>();
        stepRecordMapper.findByUserIdBetween(userId, from, today)
                .forEach(record -> steps.put(record.getRecordDate(), value(record.getSteps())));

        Map<LocalDate, Integer> water = new HashMap<>();
        waterRecordMapper.findByUserAndRecordDateBetween(userId, from, today)
                .forEach(record -> water.merge(record.getRecordDate(), value(record.getAmountMl()), Integer::sum));

        Map<LocalDate, Integer> meals = new HashMap<>();
        mealRecordMapper.findByUserAndRecordDateBetween(userId, from, today)
                .forEach(record -> meals.merge(record.getRecordDate(), 1, Integer::sum));

        List<String> labels = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            labels.add(today.minusDays(i).toString().substring(5));
        }

        List<AnalysisVO.Metric> metrics = new ArrayList<>();
        List<AnalysisVO.ChartSeries> charts = new ArrayList<>();
        List<Advice> adviceList = new ArrayList<>();
        analyzeMetric("睡眠", "分钟", sleep, today, labels, metrics, charts, adviceList);
        analyzeMetric("健身", "分钟", fitness, today, labels, metrics, charts, adviceList);
        analyzeMetric("步数", "步", steps, today, labels, metrics, charts, adviceList);
        analyzeMetric("饮水", "ml", water, today, labels, metrics, charts, adviceList);
        analyzeMetric("三餐", "餐", meals, today, labels, metrics, charts, adviceList);
        adviceList.sort(Comparator.comparingInt(Advice::rank));

        List<AnalysisVO.Suggestion> suggestions = adviceList.stream()
                .map(item -> new AnalysisVO.Suggestion(
                        item.name() + " · " + item.level(),
                        item.level(),
                        item.levelClass(),
                        item.severity(),
                        item.advice()
                ))
                .toList();
        return new AnalysisVO.Overview(labels, metrics, charts, suggestions);
    }

    private void analyzeMetric(String name, String unit, Map<LocalDate, Integer> daily, LocalDate today,
                               List<String> labels, List<AnalysisVO.Metric> metrics,
                               List<AnalysisVO.ChartSeries> charts, List<Advice> adviceList) {
        LocalDate recentFrom = today.minusDays(6);
        LocalDate baselineFrom = today.minusDays(13);
        LocalDate baselineTo = today.minusDays(7);

        double recentAverage = average(daily, recentFrom, today);
        double baselineAverage = average(daily, baselineFrom, baselineTo);
        boolean hasRecent = hasData(daily, recentFrom, today);
        boolean hasBaseline = hasData(daily, baselineFrom, baselineTo);
        boolean hasData = hasRecent && hasBaseline && baselineAverage > 0;

        double change = hasData ? (recentAverage - baselineAverage) / baselineAverage * 100.0 : 0;
        String level;
        String levelClass;
        if (!hasData) {
            level = "数据不足";
            levelClass = "gray";
        } else if (Math.abs(change) < 5) {
            level = "基本持平";
            levelClass = "gray";
        } else if (change >= 15) {
            level = "显著改善";
            levelClass = "green";
        } else if (change > 5) {
            level = "轻微改善";
            levelClass = "green";
        } else if (change <= -15) {
            level = "显著下降";
            levelClass = "red";
        } else {
            level = "轻微下降";
            levelClass = "orange";
        }

        metrics.add(new AnalysisVO.Metric(
                name,
                unit,
                hasRecent ? format(recentAverage) + " " + unit : "—",
                hasBaseline ? format(baselineAverage) + " " + unit : "—",
                hasData ? String.format("%+.1f%%", change) : "—",
                hasData && change >= 5,
                hasData && change <= -5,
                level,
                levelClass
        ));
        charts.add(new AnalysisVO.ChartSeries(
                name, unit, series(daily, today), round1(recentAverage), round1(baselineAverage)
        ));

        if ("red".equals(levelClass) || "orange".equals(levelClass)) {
            boolean severe = "red".equals(levelClass);
            adviceList.add(new Advice(
                    name,
                    level,
                    levelClass,
                    severe ? "high" : "warn",
                    severe ? 0 : 1,
                    adviceFor(name, Math.abs(change), severe)
            ));
        }
    }

    private String adviceFor(String name, double change, boolean severe) {
        String prefix = severe ? "【重点关注】" : "";
        String percent = String.format("%.0f%%", change);
        return switch (name) {
            case "睡眠" -> prefix + "近 7 天平均睡眠较前 7 天下降 " + percent
                    + "。可以固定入睡与起床时间，睡前减少屏幕使用，给身体留出稳定的恢复窗口。";
            case "健身" -> prefix + "近 7 天健身时长较前 7 天下降 " + percent
                    + "。建议从每周 3 次、每次 30 分钟开始，安排力量训练与低强度有氧的组合。";
            case "步数" -> prefix + "近期日均步数较此前下降 " + percent
                    + "。可以设置每天 6000–8000 步的小目标，用饭后散步或提前一站下车来稳定增加活动量。";
            case "饮水" -> prefix + "近期日均饮水量较此前下降 " + percent
                    + "。建议把水杯放在手边，分时段小口补水，每天以 1500–2000ml 作为参考。";
            case "三餐" -> prefix + "近期规律用餐记录较此前下降 " + percent
                    + "。可以提前安排三餐时间，优先保证早餐，并减少用零食代替正餐的情况。";
            default -> prefix + "近期这项指标有所下降，可以回看记录，为下一周设置一个容易完成的小目标。";
        };
    }

    private double average(Map<LocalDate, Integer> daily, LocalDate from, LocalDate to) {
        int sum = 0;
        int count = 0;
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            Integer value = daily.get(date);
            if (value != null) {
                sum += value;
                count++;
            }
        }
        return count == 0 ? 0 : (double) sum / count;
    }

    private boolean hasData(Map<LocalDate, Integer> daily, LocalDate from, LocalDate to) {
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (daily.containsKey(date)) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> series(Map<LocalDate, Integer> daily, LocalDate today) {
        List<Integer> values = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            values.add(daily.getOrDefault(today.minusDays(i), 0));
        }
        return values;
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String format(double value) {
        double rounded = round1(value);
        return rounded == Math.rint(rounded)
                ? String.valueOf((long) rounded)
                : String.valueOf(rounded);
    }

    private record Advice(
            String name,
            String level,
            String levelClass,
            String severity,
            int rank,
            String advice
    ) {
    }
}
