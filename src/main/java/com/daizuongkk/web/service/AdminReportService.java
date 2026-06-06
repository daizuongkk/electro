package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.request.AdminReportFilterRequest;
import com.daizuongkk.web.dto.response.InventoryReportResponse;
import com.daizuongkk.web.dto.response.InventoryReportRowResponse;
import com.daizuongkk.web.dto.response.OrderReportResponse;
import com.daizuongkk.web.dto.response.OrderReportRowResponse;
import com.daizuongkk.web.dto.response.ProductSalesReportResponse;
import com.daizuongkk.web.dto.response.ProductSalesReportRowResponse;
import com.daizuongkk.web.dto.response.RevenueReportResponse;
import com.daizuongkk.web.dto.response.RevenueReportRowResponse;
import com.daizuongkk.web.model.Category;
import com.daizuongkk.web.repository.ReportRepository;
import com.daizuongkk.web.util.ReportFormatUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdminReportService {
    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "PAID", "SHIPPED", "COMPLETED", "CANCELLED");
    private static final Set<Integer> VALID_TOP_LIMITS = Set.of(5, 10, 20, 50);

    private final ReportRepository reportRepository;

    public AdminReportService() {
        this(new ReportRepository());
    }

    public AdminReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository == null ? new ReportRepository() : reportRepository;
    }

    public AdminReportFilterRequest normalizeFilters(AdminReportFilterRequest filters) {
        AdminReportFilterRequest source = filters == null ? AdminReportFilterRequest.builder().build() : filters;
        String reportType = ReportFormatUtils.normalizeReportType(source.getReportType());
        LocalDate fromDate = source.getFromDate() == null ? LocalDate.now().minusDays(30) : source.getFromDate();
        LocalDate toDate = source.getToDate() == null ? LocalDate.now() : source.getToDate();
        if (fromDate.isAfter(toDate)) {
            LocalDate temp = fromDate;
            fromDate = toDate;
            toDate = temp;
        }

        return AdminReportFilterRequest.builder()
                .reportType(reportType)
                .fromDate(fromDate)
                .toDate(toDate)
                .status(normalizeStatus(source.getStatus()))
                .paymentMethod("")
                .topLimit(normalizeTopLimit(source.getTopLimit()))
                .exportedBy(source.getExportedBy())
                .build();
    }

    public RevenueReportResponse getRevenueReport(AdminReportFilterRequest filters) {
        AdminReportFilterRequest normalized = normalizeFilters(filters);
        List<RevenueReportRowResponse> rows = reportRepository.findRevenueRows(normalized);
        long totalOrders = rows.stream().mapToLong(row -> safeLong(row.getOrderCount())).sum();
        long totalProductsSold = rows.stream().mapToLong(row -> safeLong(row.getProductCount())).sum();
        double totalRevenue = rows.stream().mapToDouble(row -> safeDouble(row.getRevenue())).sum();

        return RevenueReportResponse.builder()
                .generatedAt(LocalDateTime.now())
                .totalOrders(totalOrders)
                .totalProductsSold(totalProductsSold)
                .totalRevenue(totalRevenue)
                .averageOrderValue(totalOrders > 0 ? totalRevenue / totalOrders : 0D)
                .rows(rows)
                .build();
    }

    public OrderReportResponse getOrderReport(AdminReportFilterRequest filters) {
        AdminReportFilterRequest normalized = normalizeFilters(filters);
        List<OrderReportRowResponse> rows = reportRepository.findOrderRows(normalized, 10000);
        double totalAmount = rows.stream().mapToDouble(row -> safeDouble(row.getTotalAmount())).sum();

        return OrderReportResponse.builder()
                .generatedAt(LocalDateTime.now())
                .totalOrders((long) rows.size())
                .totalAmount(totalAmount)
                .rows(rows)
                .build();
    }

    public ProductSalesReportResponse getProductSalesReport(AdminReportFilterRequest filters) {
        AdminReportFilterRequest normalized = normalizeFilters(filters);
        List<ProductSalesReportRowResponse> rows = reportRepository.findTopSellingProducts(normalized, normalized.getTopLimit());
        for (ProductSalesReportRowResponse row : rows) {
            row.setCategory(Category.getNameByCode(row.getCategory()));
        }

        long totalQuantitySold = rows.stream().mapToLong(row -> safeLong(row.getQuantitySold())).sum();
        double totalRevenue = rows.stream().mapToDouble(row -> safeDouble(row.getRevenue())).sum();

        return ProductSalesReportResponse.builder()
                .generatedAt(LocalDateTime.now())
                .topLimit(normalized.getTopLimit())
                .totalQuantitySold(totalQuantitySold)
                .totalRevenue(totalRevenue)
                .rows(rows)
                .build();
    }

    public InventoryReportResponse getInventoryReport() {
        List<InventoryReportRowResponse> rows = reportRepository.findInventoryRows();
        for (InventoryReportRowResponse row : rows) {
            row.setCategory(Category.getNameByCode(row.getCategory()));
        }

        long outOfStock = rows.stream()
                .filter(row -> "Hết hàng".equals(row.getStockStatus()))
                .count();
        long lowStock = rows.stream()
                .filter(row -> "Sắp hết hàng".equals(row.getStockStatus()))
                .count();
        long inStock = rows.stream()
                .filter(row -> "Còn hàng".equals(row.getStockStatus()))
                .count();
        double inventoryValue = rows.stream()
                .mapToDouble(row -> safeLong(row.getQuantity()) * safeDouble(row.getPrice()))
                .sum();

        return InventoryReportResponse.builder()
                .generatedAt(LocalDateTime.now())
                .totalProducts((long) rows.size())
                .outOfStockProducts(outOfStock)
                .lowStockProducts(lowStock)
                .inStockProducts(inStock)
                .inventoryValue(inventoryValue)
                .rows(rows)
                .build();
    }

    public Map<String, String> getStatusOptions() {
        Map<String, String> statuses = new LinkedHashMap<>();
        statuses.put("PENDING", ReportFormatUtils.statusLabel("PENDING"));
        statuses.put("PAID", ReportFormatUtils.statusLabel("PAID"));
        statuses.put("SHIPPED", ReportFormatUtils.statusLabel("SHIPPED"));
        statuses.put("COMPLETED", ReportFormatUtils.statusLabel("COMPLETED"));
        statuses.put("CANCELLED", ReportFormatUtils.statusLabel("CANCELLED"));
        return statuses;
    }

    public List<Integer> getTopLimitOptions() {
        return List.of(5, 10, 20, 50);
    }

    public String getReportTitle(String reportType) {
        return ReportFormatUtils.reportTitle(reportType);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }

        String normalized = status.trim().toUpperCase();
        return VALID_STATUSES.contains(normalized) ? normalized : "";
    }

    private Integer normalizeTopLimit(Integer topLimit) {
        if (topLimit == null || !VALID_TOP_LIMITS.contains(topLimit)) {
            return 10;
        }
        return topLimit;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0D : value;
    }
}
