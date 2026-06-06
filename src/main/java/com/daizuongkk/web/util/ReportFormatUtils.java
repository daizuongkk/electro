package com.daizuongkk.web.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public final class ReportFormatUtils {
    private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReportFormatUtils() {
    }

    public static String normalizeReportType(String value) {
        if (value == null || value.isBlank()) {
            return "revenue";
        }

        return switch (value.trim().toLowerCase()) {
            case "orders", "products", "inventory" -> value.trim().toLowerCase();
            default -> "revenue";
        };
    }

    public static String reportTitle(String reportType) {
        return switch (normalizeReportType(reportType)) {
            case "orders" -> "Báo cáo đơn hàng";
            case "products" -> "Báo cáo sản phẩm bán chạy";
            case "inventory" -> "Báo cáo tồn kho";
            default -> "Báo cáo doanh thu";
        };
    }

    public static String filePrefix(String reportType) {
        return switch (normalizeReportType(reportType)) {
            case "orders" -> "BaoCaoDonHang";
            case "products" -> "BaoCaoSanPhamBanChay";
            case "inventory" -> "BaoCaoTonKho";
            default -> "BaoCaoDoanhThu";
        };
    }

    public static String exportedBy(String value) {
        return value == null || value.isBlank() ? "Quản trị viên" : value.trim();
    }

    public static String statusLabel(String status) {
        if (status == null || status.isBlank()) {
            return "Tất cả";
        }

        return switch (status.trim().toUpperCase()) {
            case "PENDING" -> "Chờ xử lý";
            case "PAID" -> "Đã thanh toán";
            case "SHIPPED" -> "Đang giao";
            case "COMPLETED" -> "Hoàn tất";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    public static String stockStatus(Long quantity) {
        long safeQuantity = quantity == null ? 0L : quantity;
        if (safeQuantity == 0) {
            return "Hết hàng";
        }
        if (safeQuantity <= 10) {
            return "Sắp hết hàng";
        }
        return "Còn hàng";
    }

    public static String paymentMethodFromAddress(String address) {
        if (address == null || address.isBlank()) {
            return "Không xác định";
        }

        String marker = "Thanh toán:";
        int markerIndex = address.indexOf(marker);
        if (markerIndex < 0) {
            return "Không xác định";
        }

        String value = address.substring(markerIndex + marker.length()).trim();
        int separatorIndex = value.indexOf('|');
        if (separatorIndex >= 0) {
            value = value.substring(0, separatorIndex).trim();
        }
        return value.isBlank() ? "Không xác định" : value;
    }

    public static String formatCurrency(Double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(VIETNAM_LOCALE);
        return formatter.format(value == null ? 0D : value);
    }

    public static String formatNumber(Number value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(VIETNAM_LOCALE);
        return formatter.format(value == null ? 0 : value);
    }

    public static String formatDate(LocalDate value) {
        return value == null ? "-" : value.format(DATE_FORMATTER);
    }

    public static String formatDate(Date value) {
        if (value == null) {
            return "-";
        }
        return DATE_FORMATTER.format(value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public static String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
    }
}
