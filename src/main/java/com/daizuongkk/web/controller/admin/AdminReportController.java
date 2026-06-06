package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.dto.request.AdminReportFilterRequest;
import com.daizuongkk.web.dto.response.InventoryReportResponse;
import com.daizuongkk.web.dto.response.OrderReportResponse;
import com.daizuongkk.web.dto.response.ProductSalesReportResponse;
import com.daizuongkk.web.dto.response.RevenueReportResponse;
import com.daizuongkk.web.dto.response.UserResponse;
import com.daizuongkk.web.service.AdminReportService;
import com.daizuongkk.web.util.ReportExcelExporter;
import com.daizuongkk.web.util.ReportFormatUtils;
import com.daizuongkk.web.util.ReportPdfExporter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(name = "AdminReportController", value = {
        "/admin/reports",
        "/admin/reports/revenue",
        "/admin/reports/orders",
        "/admin/reports/products",
        "/admin/reports/inventory",
        "/admin/reports/export/excel",
        "/admin/reports/export/pdf"
})
public class AdminReportController extends BaseAdminServlet {
    private final AdminReportService reportService = new AdminReportService();
    private final ReportExcelExporter excelExporter = new ReportExcelExporter();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        AdminReportFilterRequest filters = reportService.normalizeFilters(buildFilters(request));
        filters.setExportedBy(resolveAdminName(request));
        String servletPath = request.getServletPath();
        if (servletPath.endsWith("/export/excel")) {
            exportExcel(response, filters);
            return;
        }
        if (servletPath.endsWith("/export/pdf")) {
            exportPdf(response, filters);
            return;
        }

        loadReport(request, filters);
        forward(request, response, "admin-reports.jsp");
    }

    private AdminReportFilterRequest buildFilters(HttpServletRequest request) {
        String reportType = resolveReportType(request);
        return AdminReportFilterRequest.builder()
                .reportType(reportType)
                .fromDate(parseDate(request.getParameter("fromDate")))
                .toDate(parseDate(request.getParameter("toDate")))
                .status(trim(request.getParameter("status")))
                .paymentMethod(trim(request.getParameter("paymentMethod")))
                .topLimit(parseTopLimit(request.getParameter("topLimit")))
                .build();
    }

    private void loadReport(HttpServletRequest request, AdminReportFilterRequest filters) {
        request.setAttribute("filter", filters);
        request.setAttribute("reportType", filters.getReportType());
        request.setAttribute("reportTitle", reportService.getReportTitle(filters.getReportType()));
        request.setAttribute("statusOptions", reportService.getStatusOptions());
        request.setAttribute("topLimitOptions", reportService.getTopLimitOptions());
        request.setAttribute("paymentMethodAvailable", false);

        switch (filters.getReportType()) {
            case "orders" -> request.setAttribute("orderReport", reportService.getOrderReport(filters));
            case "products" -> request.setAttribute("productSalesReport", reportService.getProductSalesReport(filters));
            case "inventory" -> request.setAttribute("inventoryReport", reportService.getInventoryReport());
            default -> request.setAttribute("revenueReport", reportService.getRevenueReport(filters));
        }
    }

    private void exportExcel(HttpServletResponse response, AdminReportFilterRequest filters) throws IOException {
        String filename = buildFilename(filters.getReportType(), "xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        setDownloadHeader(response, filename);

        switch (filters.getReportType()) {
            case "orders" -> excelExporter.exportOrders(reportService.getOrderReport(filters), filters, response.getOutputStream());
            case "products" -> excelExporter.exportProducts(reportService.getProductSalesReport(filters), filters, response.getOutputStream());
            case "inventory" -> excelExporter.exportInventory(reportService.getInventoryReport(), filters, response.getOutputStream());
            default -> excelExporter.exportRevenue(reportService.getRevenueReport(filters), filters, response.getOutputStream());
        }
    }

    private void exportPdf(HttpServletResponse response, AdminReportFilterRequest filters) throws IOException {
        String filename = buildFilename(filters.getReportType(), "pdf");
        response.setContentType("application/pdf");
        setDownloadHeader(response, filename);

        ReportPdfExporter pdfExporter = new ReportPdfExporter(
                getPdfFontPath("Roboto-Regular.ttf"),
                getPdfFontPath("Roboto-SemiBold.ttf"),
                getPdfFontPath("Roboto-Bold.ttf"));
        switch (filters.getReportType()) {
            case "orders" -> {
                OrderReportResponse report = reportService.getOrderReport(filters);
                pdfExporter.exportOrders(report, filters, response.getOutputStream());
            }
            case "products" -> {
                ProductSalesReportResponse report = reportService.getProductSalesReport(filters);
                pdfExporter.exportProducts(report, filters, response.getOutputStream());
            }
            case "inventory" -> {
                InventoryReportResponse report = reportService.getInventoryReport();
                pdfExporter.exportInventory(report, filters, response.getOutputStream());
            }
            default -> {
                RevenueReportResponse report = reportService.getRevenueReport(filters);
                pdfExporter.exportRevenue(report, filters, response.getOutputStream());
            }
        }
    }

    private String resolveReportType(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath.endsWith("/orders")) {
            return "orders";
        }
        if (servletPath.endsWith("/products")) {
            return "products";
        }
        if (servletPath.endsWith("/inventory")) {
            return "inventory";
        }
        if (servletPath.endsWith("/revenue")) {
            return "revenue";
        }
        return ReportFormatUtils.normalizeReportType(request.getParameter("type"));
    }

    private LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseTopLimit(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String buildFilename(String reportType, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return ReportFormatUtils.filePrefix(reportType) + "_" + timestamp + "." + extension;
    }

    private void setDownloadHeader(HttpServletResponse response, String filename) {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''"
                + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"));
    }

    private String getPdfFontPath(String fileName) {
        return getServletContext().getRealPath("/assets/fonts/Poppins/" + fileName);
    }

    private String resolveAdminName(HttpServletRequest request) {
        Object account = request.getSession().getAttribute("account");
        if (!(account instanceof UserResponse user)) {
            return "Quản trị viên";
        }

        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        return user.getUsername() == null || user.getUsername().isBlank() ? "Quản trị viên" : user.getUsername();
    }
}
