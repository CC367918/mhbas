package com.mhbas.service.impl;

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
import com.mhbas.service.GoalService;
import com.mhbas.service.ReportService;
import com.mhbas.vo.GoalVO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {
    private static final String TEMPLATE_PATH = "report/weekly-health-report-template.xlsx";
    private static final DateTimeFormatter REPORT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);
    private static final DateTimeFormatter GENERATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CHINA);

    // 每日数据表格与个性化建议在模板中的起始行
    private static final int DATA_START_ROW = 10;
    private static final int ADVICE_START_ROW = 19;
    private static final int ADVICE_SLOTS = 5;
    // 系统覆盖的健康维度数：睡眠、健身、步数、饮水、三餐
    private static final int TRACKED_DIMENSIONS = 5;

    private final SleepRecordMapper sleepRecordMapper;
    private final FitnessRecordMapper fitnessRecordMapper;
    private final StepRecordMapper stepRecordMapper;
    private final WaterRecordMapper waterRecordMapper;
    private final MealRecordMapper mealRecordMapper;
    private final GoalService goalService;

    public ReportServiceImpl(SleepRecordMapper sleepRecordMapper,
                             FitnessRecordMapper fitnessRecordMapper,
                             StepRecordMapper stepRecordMapper,
                             WaterRecordMapper waterRecordMapper,
                             MealRecordMapper mealRecordMapper,
                             GoalService goalService) {
        this.sleepRecordMapper = sleepRecordMapper;
        this.fitnessRecordMapper = fitnessRecordMapper;
        this.stepRecordMapper = stepRecordMapper;
        this.waterRecordMapper = waterRecordMapper;
        this.mealRecordMapper = mealRecordMapper;
        this.goalService = goalService;
    }

    /**
     * 导出并下载当前用户最近 7 个完整自然日的 Excel 健康周报。
     */
    @Override
    public void exportWeeklyReport(Long userId, HttpServletResponse response) throws IOException {
        WeeklyReportData reportData = loadReportData(userId);
        byte[] report = fillTemplate(reportData);
        String fileName = "健康周报-" + reportData.startDate().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + reportData.endDate().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";

        response.reset();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentLength(report.length);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
                        .toString());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.getOutputStream().write(report);
        response.flushBuffer();
    }

    // 一次查询并聚合睡眠、健身、步数、饮水、三餐五类记录
    private WeeklyReportData loadReportData(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today.minusDays(1);

        Map<LocalDate, Integer> sleepMinutes = new TreeMap<>();
        for (SleepRecord r : sleepRecordMapper.findByUserAndRecordDateBetween(userId, startDate, endDate)) {
            sleepMinutes.merge(r.getRecordDate(), nz(r.getDurationMinutes()), Integer::sum);
        }

        Map<LocalDate, Integer> waterMl = new TreeMap<>();
        for (WaterRecord r : waterRecordMapper.findByUserAndRecordDateBetween(userId, startDate, endDate)) {
            waterMl.merge(r.getRecordDate(), nz(r.getAmountMl()), Integer::sum);
        }

        Map<LocalDate, Integer> mealCount = new TreeMap<>();
        for (MealRecord r : mealRecordMapper.findByUserAndRecordDateBetween(userId, startDate, endDate)) {
            mealCount.merge(r.getRecordDate(), 1, Integer::sum);
        }

        Map<LocalDate, Integer> steps = new TreeMap<>();
        for (StepRecord r : stepRecordMapper.findByUserIdBetween(userId, startDate, endDate)) {
            steps.merge(r.getRecordDate(), nz(r.getSteps()), Integer::sum);
        }

        Map<LocalDate, Integer> fitnessMinutes = new TreeMap<>();
        Map<LocalDate, Integer> fitnessCount = new TreeMap<>();
        for (FitnessRecord r : fitnessRecordMapper.findByUserAndStartTimeBetween(
                userId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay())) {
            if (r.getStartTime() == null) {
                continue;
            }
            LocalDate date = r.getStartTime().toLocalDate();
            if (date.isBefore(startDate) || date.isAfter(endDate)) {
                continue;
            }
            fitnessMinutes.merge(date, nz(r.getDurationMinutes()), Integer::sum);
            fitnessCount.merge(date, 1, Integer::sum);
        }

        List<DailyReport> days = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            days.add(new DailyReport(
                    date,
                    sleepMinutes.getOrDefault(date, 0),
                    fitnessCount.getOrDefault(date, 0),
                    fitnessMinutes.getOrDefault(date, 0),
                    steps.getOrDefault(date, 0),
                    waterMl.getOrDefault(date, 0),
                    mealCount.getOrDefault(date, 0)
            ));
        }

        int[] goalStats = goalStats(userId);
        return new WeeklyReportData(startDate, endDate, List.copyOf(days), goalStats[0], goalStats[1]);
    }

    // 统计已启用目标中的达标数量：返回 [达标数, 已启用总数]
    private int[] goalStats(Long userId) {
        List<GoalVO.Detail> enabled = goalService.listWithProgress(userId).stream()
                .filter(GoalVO.Detail::enabled)
                .toList();
        int achieved = (int) enabled.stream().filter(GoalVO.Detail::achieved).count();
        return new int[]{achieved, enabled.size()};
    }

    // 填写统计周期、生成时间、本周摘要、每日数据及个性化建议
    private byte[] fillTemplate(WeeklyReportData data) throws IOException {
        ClassPathResource template = new ClassPathResource(TEMPLATE_PATH);
        try (InputStream input = template.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(input);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.getSheetAt(0);

            setText(sheet, 1, 0, "统计周期："
                    + data.startDate().format(REPORT_DATE_FORMATTER)
                    + " 至 " + data.endDate().format(REPORT_DATE_FORMATTER)
                    + "（最近 7 个完整自然日）");
            setText(sheet, 2, 0, "生成时间：" + LocalDateTime.now().format(GENERATED_AT_FORMATTER));

            fillSummary(sheet, data);
            fillDailyData(sheet, data);

            List<String> advice = buildAdvice(data);
            for (int i = 0; i < ADVICE_SLOTS; i++) {
                setText(sheet, ADVICE_START_ROW + i, 0,
                        i < advice.size() ? (i + 1) + ". " + advice.get(i) : "");
            }

            workbook.setForceFormulaRecalculation(true);
            workbook.write(output);
            return output.toByteArray();
        }
    }

    // 本周摘要：按标签文字定位其右侧单元格写入数值，避免硬编码行列导致错位
    private void fillSummary(Sheet sheet, WeeklyReportData data) {
        double avgSleepHours = round1(data.totalSleepMinutes() / 7.0 / 60.0);
        int totalFitnessMinutes = data.totalFitnessMinutes();
        double avgSteps = round1(data.totalSteps() / 7.0);
        double avgWaterMl = round1(data.totalWaterMl() / 7.0);
        int totalFitnessCount = data.totalFitnessCount();
        int totalMealCount = data.totalMealCount();
        int daysWithRecords = data.daysWithRecords();

        writeSummaryValue(sheet, "睡眠日均", String.valueOf(avgSleepHours));
        writeSummaryValue(sheet, "健身总量", String.valueOf(totalFitnessMinutes));
        writeSummaryValue(sheet, "步数日均", String.valueOf(avgSteps));
        writeSummaryValue(sheet, "饮水日均", String.valueOf(avgWaterMl));

        writeSummaryValue(sheet, "健身次数", String.valueOf(totalFitnessCount));
        writeSummaryValue(sheet, "三餐记录", String.valueOf(totalMealCount));
        writeSummaryValue(sheet, "有记录天数", daysWithRecords + " / 7 天");
        writeSummaryValue(sheet, "本周达标项", data.achievedGoals() + " / " + data.totalGoals() + " 项");
    }

    private void fillDailyData(Sheet sheet, WeeklyReportData data) {
        for (int i = 0; i < data.days().size(); i++) {
            DailyReport day = data.days().get(i);
            Row row = sheet.getRow(DATA_START_ROW + i);
            if (row == null) {
                continue;
            }
            cell(row, 0).setCellValue(day.date().toString());
            cell(row, 1).setCellValue(round1(day.sleepMinutes() / 60.0));
            cell(row, 2).setCellValue(day.fitnessCount());
            cell(row, 3).setCellValue(day.fitnessMinutes());
            cell(row, 4).setCellValue(day.steps());
            cell(row, 5).setCellValue(day.waterMl());
            cell(row, 6).setCellValue(day.mealCount());
            cell(row, 7).setCellValue(day.hasRecord() ? "已记录" : "待记录");
        }
    }

    // 基于本周数据生成个性化建议，最多 TRACKED_DIMENSIONS 条
    private List<String> buildAdvice(WeeklyReportData data) {
        List<String> advice = new ArrayList<>();
        double avgSleepHours = data.totalSleepMinutes() / 7.0 / 60.0;
        double avgSteps = data.totalSteps() / 7.0;
        double avgWaterMl = data.totalWaterMl() / 7.0;

        if (avgSleepHours < 7) {
            advice.add("本周日均睡眠约 " + round1(avgSleepHours) + " 小时，低于建议的 7–9 小时。"
                    + "可以固定入睡与起床时间，睡前减少屏幕使用，给身体留出稳定的恢复窗口。");
        }
        if (data.totalFitnessMinutes() < 150) {
            advice.add("本周健身总时长为 " + data.totalFitnessMinutes() + " 分钟，未达到建议的 150 分钟。"
                    + "建议从每周 3 次、每次 30 分钟开始，安排有氧与力量训练的组合。");
        }
        if (avgSteps < 6000) {
            advice.add("本周日均步数约 " + round1(avgSteps) + " 步，活动量偏少。"
                    + "可以设置每天 6000–8000 步的小目标，用饭后散步或提前一站下车来稳定增加活动量。");
        }
        if (avgWaterMl < 1500) {
            advice.add("本周日均饮水约 " + round1(avgWaterMl) + " ml，偏低。"
                    + "建议把水杯放在手边，分时段小口补水，每天以 1500–2000ml 作为参考。");
        }
        if (data.totalMealCount() < 21) {
            advice.add("本周共记录 " + data.totalMealCount() + " 次用餐，规律三餐尚未坚持。"
                    + "可以提前安排三餐时间，优先保证早餐，减少用零食代替正餐的情况。");
        }
        if (data.daysWithRecords() < 7) {
            advice.add("本周仅有 " + data.daysWithRecords() + " 天留下了健康记录。"
                    + "建议每天固定时间记录，完整的数据能让分析和建议更准确。");
        }
        if (advice.isEmpty()) {
            advice.add("本周各项健康指标保持良好，睡眠、健身、步数、饮水与三餐都比较规律，继续保持！");
        }
        return advice.size() > TRACKED_DIMENSIONS ? advice.subList(0, TRACKED_DIMENSIONS) : advice;
    }
    // 填写摘要
    private void writeSummaryValue(Sheet sheet, String label, String value) {
        for (Row row : sheet) {
            for (Cell c : row) {
                if (c.getCellType() == CellType.STRING && label.equals(c.getStringCellValue().trim())) {
                    int targetIndex = c.getColumnIndex() + 1;
                    Cell target = row.getCell(targetIndex);
                    if (target == null) {
                        target = row.createCell(targetIndex);
                    }
                    target.setCellValue(value);
                    return;
                }
            }
        }
    }

    // 设置文本
    private void setText(Sheet sheet, int rowIndex, int columnIndex, String value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        cell(row, columnIndex).setCellValue(value);
    }

    private Cell cell(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        return cell == null ? row.createCell(columnIndex) : cell;
    }
    // 处理空值
    private int nz(Integer value) {
        return value == null ? 0 : value;
    }

    // 四舍五入
    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    // 每日数据
    private record DailyReport(LocalDate date,
                               int sleepMinutes,
                               int fitnessCount,
                               int fitnessMinutes,
                               int steps,
                               int waterMl,
                               int mealCount) {
        boolean hasRecord() {
            return sleepMinutes > 0 || fitnessCount > 0 || steps > 0
                    || waterMl > 0 || mealCount > 0;
        }
    }

    private record WeeklyReportData(LocalDate startDate,
                                    LocalDate endDate,
                                    List<DailyReport> days,
                                    int achievedGoals,
                                    int totalGoals) {
        int daysWithRecords() {
            return (int) days.stream().filter(DailyReport::hasRecord).count();
        }

        int totalSleepMinutes() {
            return days.stream().mapToInt(DailyReport::sleepMinutes).sum();
        }

        int totalFitnessCount() {
            return days.stream().mapToInt(DailyReport::fitnessCount).sum();
        }

        int totalFitnessMinutes() {
            return days.stream().mapToInt(DailyReport::fitnessMinutes).sum();
        }

        int totalSteps() {
            return days.stream().mapToInt(DailyReport::steps).sum();
        }

        int totalWaterMl() {
            return days.stream().mapToInt(DailyReport::waterMl).sum();
        }

        int totalMealCount() {
            return days.stream().mapToInt(DailyReport::mealCount).sum();
        }
    }
}