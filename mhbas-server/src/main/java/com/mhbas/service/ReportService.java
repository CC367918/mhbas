package com.mhbas.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface ReportService {
    void exportWeeklyReport(Long userId, HttpServletResponse response) throws IOException;
}
