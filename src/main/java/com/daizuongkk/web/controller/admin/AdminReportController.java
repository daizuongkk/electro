package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.dto.response.DashboardReportResponse;
import com.daizuongkk.web.service.DashboardReportExcelExporter;
import com.daizuongkk.web.service.DashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebServlet(name = "AdminReportController", value = "/admin/report")
public class AdminReportController extends BaseAdminServlet {
	private final DashboardService dashboardService = new DashboardService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!requireAdmin(request, response)) {
			return;
		}

		LocalDate fromDate = parseDate(request.getParameter("fromDate"), LocalDate.now().minusDays(30));
		LocalDate toDate = parseDate(request.getParameter("toDate"), LocalDate.now());
		DashboardReportResponse report = dashboardService.getReport(fromDate, toDate);

		String filename = "bao-cao-dashboard-" + report.getFromDate() + "-den-" + report.getToDate() + ".xlsx";
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''"
				+ URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"));

		DashboardReportExcelExporter exporter = new DashboardReportExcelExporter();
		exporter.exportDashboard(report, response.getOutputStream());
	}

	private LocalDate parseDate(String value, LocalDate fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}

		try {
			return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
		} catch (Exception e) {
			return fallback;
		}
	}
}
