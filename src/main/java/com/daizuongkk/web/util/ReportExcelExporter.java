package com.daizuongkk.web.util;

import com.daizuongkk.web.dto.request.AdminReportFilterRequest;
import com.daizuongkk.web.dto.response.InventoryReportResponse;
import com.daizuongkk.web.dto.response.InventoryReportRowResponse;
import com.daizuongkk.web.dto.response.OrderReportResponse;
import com.daizuongkk.web.dto.response.OrderReportRowResponse;
import com.daizuongkk.web.dto.response.ProductSalesReportResponse;
import com.daizuongkk.web.dto.response.ProductSalesReportRowResponse;
import com.daizuongkk.web.dto.response.RevenueReportResponse;
import com.daizuongkk.web.dto.response.RevenueReportRowResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.Grouping;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFPieChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportExcelExporter {
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");
    private static final String OVERVIEW_SHEET = "Tổng quan";
    private static final String DETAIL_SHEET = "Dữ liệu chi tiết";
    private static final String AUTO_NOTE = "Báo cáo được tạo tự động từ hệ thống Electro";
    private static final String BRAND_RED = "D10024";
    private static final String BRAND_ORANGE = "E66239";
    private static final String BRAND_DARK = "302C4D";
    private static final String BRAND_TEXT = "2B2D42";
    private static final String BRAND_MUTED = "657E92";
    private static final String BRAND_LIGHT = "FBFBFC";
    private static final String BRAND_BORDER = "E4E7ED";
    private static final String BRAND_SUBTLE = "FAE0D7";
    private static final String BRAND_TOTAL = "FFF4E8";
    private static final String CHART_GREEN = "00C951";
    private static final String CHART_BLUE = "00B8DB";
    private static final String CHART_YELLOW = "F0B100";
    private static final String CHART_DANGER = "FB2C36";
    private static final String[] CHART_COLORS = {
            BRAND_RED, BRAND_ORANGE, BRAND_DARK, BRAND_MUTED, CHART_GREEN, CHART_BLUE, CHART_YELLOW, CHART_DANGER
    };

    public void exportRevenue(RevenueReportResponse report, AdminReportFilterRequest filters, OutputStream outputStream)
            throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Styles styles = new Styles(workbook);
            XSSFSheet overview = workbook.createSheet(OVERVIEW_SHEET);
            XSSFSheet detail = workbook.createSheet(DETAIL_SHEET);

            writeOverviewHeader(overview, styles, ReportFormatUtils.reportTitle("revenue"), filters);
            writeKpiCards(overview, styles,
                    new Kpi("Tổng doanh thu", ReportFormatUtils.formatCurrency(report.getTotalRevenue())),
                    new Kpi("Tổng số đơn hàng", ReportFormatUtils.formatNumber(report.getTotalOrders())),
                    new Kpi("Tổng số sản phẩm đã bán", ReportFormatUtils.formatNumber(report.getTotalProductsSold())),
                    new Kpi("Giá trị đơn hàng trung bình", ReportFormatUtils.formatCurrency(report.getAverageOrderValue())));
            int lastChartRow = writeRevenueChartData(overview, report, styles, 15, 0);
            createLineChart(overview, "Doanh thu theo thời gian", 0, 1, 16, lastChartRow, 4, 14, 11, 31);

            int footerRow = writeRevenueDetail(detail, report, filters, styles);
            writeFooter(detail, styles, footerRow, "Tổng doanh thu trong kỳ: "
                    + ReportFormatUtils.formatCurrency(report.getTotalRevenue()), filters, 4);
            finishSheet(overview, 11);
            finishSheet(detail, 5);
            workbook.write(outputStream);
        }
    }

    public void exportOrders(OrderReportResponse report, AdminReportFilterRequest filters, OutputStream outputStream)
            throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Styles styles = new Styles(workbook);
            XSSFSheet overview = workbook.createSheet(OVERVIEW_SHEET);
            XSSFSheet detail = workbook.createSheet(DETAIL_SHEET);

            writeOverviewHeader(overview, styles, ReportFormatUtils.reportTitle("orders"), filters);
            writeKpiCards(overview, styles,
                    new Kpi("Tổng số đơn hàng", ReportFormatUtils.formatNumber(report.getTotalOrders())),
                    new Kpi("Tổng giá trị đơn hàng", ReportFormatUtils.formatCurrency(report.getTotalAmount())),
                    new Kpi("Trạng thái đang lọc", ReportFormatUtils.statusLabel(filters.getStatus())),
                    new Kpi("Nguồn dữ liệu", "Dữ liệu đơn hàng"));
            int lastChartRow = writeOrderStatusChartData(overview, report, styles, 15, 0);
            createPieChart(overview, "Tỷ lệ đơn hàng theo trạng thái", 0, 1, 16, lastChartRow, 4, 14, 11, 31);

            int footerRow = writeOrderDetail(detail, report, filters, styles);
            writeFooter(detail, styles, footerRow, "Tổng số đơn hàng phù hợp: "
                    + ReportFormatUtils.formatNumber(report.getTotalOrders()), filters, 6);
            finishSheet(overview, 11);
            finishSheet(detail, 6);
            workbook.write(outputStream);
        }
    }

    public void exportProducts(ProductSalesReportResponse report, AdminReportFilterRequest filters,
                               OutputStream outputStream) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Styles styles = new Styles(workbook);
            XSSFSheet overview = workbook.createSheet(OVERVIEW_SHEET);
            XSSFSheet detail = workbook.createSheet(DETAIL_SHEET);

            writeOverviewHeader(overview, styles, ReportFormatUtils.reportTitle("products"), filters);
            writeKpiCards(overview, styles,
                    new Kpi("Số sản phẩm hiển thị", ReportFormatUtils.formatNumber(safeList(report.getRows()).size())),
                    new Kpi("Tổng số lượng đã bán", ReportFormatUtils.formatNumber(report.getTotalQuantitySold())),
                    new Kpi("Tổng doanh thu", ReportFormatUtils.formatCurrency(report.getTotalRevenue())),
                    new Kpi("Giới hạn báo cáo", report.getTopLimit() + " sản phẩm"));
            int lastChartRow = writeProductChartData(overview, report, styles, 15, 0);
            createBarChart(overview, "Sản phẩm bán chạy theo số lượng", 0, 1, 16, lastChartRow, 4, 14, 11, 31,
                    BarDirection.COL);

            int footerRow = writeProductDetail(detail, report, filters, styles);
            writeFooter(detail, styles, footerRow, "Tổng số lượng sản phẩm đã bán: "
                    + ReportFormatUtils.formatNumber(report.getTotalQuantitySold()), filters, 5);
            finishSheet(overview, 11);
            finishSheet(detail, 5);
            workbook.write(outputStream);
        }
    }

    public void exportInventory(InventoryReportResponse report, AdminReportFilterRequest filters,
                                OutputStream outputStream) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Styles styles = new Styles(workbook);
            XSSFSheet overview = workbook.createSheet(OVERVIEW_SHEET);
            XSSFSheet detail = workbook.createSheet(DETAIL_SHEET);

            writeOverviewHeader(overview, styles, ReportFormatUtils.reportTitle("inventory"), filters);
            writeKpiCards(overview, styles,
                    new Kpi("Tổng sản phẩm", ReportFormatUtils.formatNumber(report.getTotalProducts())),
                    new Kpi("Còn hàng", ReportFormatUtils.formatNumber(report.getInStockProducts())),
                    new Kpi("Sắp hết hàng", ReportFormatUtils.formatNumber(report.getLowStockProducts())),
                    new Kpi("Hết hàng", ReportFormatUtils.formatNumber(report.getOutOfStockProducts())));
            int lastChartRow = writeInventoryChartData(overview, report, styles, 15, 0);
            createBarChart(overview, "Số sản phẩm theo trạng thái tồn kho", 0, 1, 16, lastChartRow, 4, 14, 11, 31,
                    BarDirection.COL);

            int footerRow = writeInventoryDetail(detail, report, filters, styles);
            writeFooter(detail, styles, footerRow, "Giá trị tồn kho ước tính: "
                    + ReportFormatUtils.formatCurrency(report.getInventoryValue()), filters, 6);
            finishSheet(overview, 11);
            finishSheet(detail, 6);
            workbook.write(outputStream);
        }
    }

    private void writeOverviewHeader(XSSFSheet sheet, Styles styles, String title, AdminReportFilterRequest filters) {
        sheet.setDisplayGridlines(false);
        setWidths(sheet, 16, 20, 20, 4, 18, 18, 18, 18, 18, 18, 18);

        Row brandRow = sheet.createRow(0);
        brandRow.setHeightInPoints(24);
        writeText(brandRow, 0, "ELECTRO", styles.brand);
        merge(sheet, 0, 0, 0, 10);

        Row titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(30);
        writeText(titleRow, 0, uppercaseTitle(title), styles.title);
        merge(sheet, 1, 1, 0, 10);

        writeMeta(sheet, styles, 3, "Khoảng thời gian lọc", buildPeriod(filters));
        writeMeta(sheet, styles, 4, "Từ ngày", buildFromDate(filters));
        writeMeta(sheet, styles, 5, "Đến ngày", buildToDate(filters));
        writeMeta(sheet, styles, 6, "Ngày xuất báo cáo", ReportFormatUtils.formatDateTime(LocalDateTime.now()));
        writeMeta(sheet, styles, 7, "Người lập báo cáo", ReportFormatUtils.exportedBy(filters.getExportedBy()));
        writeMeta(sheet, styles, 8, "Ghi chú trạng thái đơn hàng", buildStatusNote(filters));
    }

    private void writeKpiCards(XSSFSheet sheet, Styles styles, Kpi... kpis) {
        int[] columns = {0, 3, 6, 9};
        for (int i = 0; i < kpis.length; i++) {
            int col = columns[i];
            Row labelRow = getOrCreateRow(sheet, 9);
            Row valueRow = getOrCreateRow(sheet, 10);
            writeText(labelRow, col, kpis[i].label(), styles.kpiLabel);
            writeText(valueRow, col, kpis[i].value(), styles.kpiValue);
            merge(sheet, 9, 9, col, Math.min(col + 1, 10));
            merge(sheet, 10, 10, col, Math.min(col + 1, 10));
        }
    }

    private int writeRevenueDetail(XSSFSheet sheet, RevenueReportResponse report, AdminReportFilterRequest filters,
                                   Styles styles) {
        int rowIndex = writeDetailHeader(sheet, styles, ReportFormatUtils.reportTitle("revenue"), filters, 4);
        writeTableHeader(sheet, rowIndex++, styles, "STT", "Ngày", "Số đơn hàng", "Số sản phẩm đã bán", "Doanh thu");
        int index = 1;
        for (RevenueReportRowResponse row : safeList(report.getRows())) {
            Row excelRow = sheet.createRow(rowIndex++);
            boolean even = index % 2 == 0;
            writeNumber(excelRow, 0, index, even ? styles.altCenter : styles.center);
            writeText(excelRow, 1, ReportFormatUtils.formatDate(row.getReportDate()), even ? styles.altCenter : styles.center);
            writeNumber(excelRow, 2, safeLong(row.getOrderCount()), even ? styles.altInteger : styles.integer);
            writeNumber(excelRow, 3, safeLong(row.getProductCount()), even ? styles.altInteger : styles.integer);
            writeNumber(excelRow, 4, safeDouble(row.getRevenue()), even ? styles.altCurrency : styles.currency);
            index++;
        }
        Row totalRow = sheet.createRow(rowIndex++);
        writeText(totalRow, 0, "Tổng cộng", styles.total);
        merge(sheet, rowIndex - 1, rowIndex - 1, 0, 1);
        writeNumber(totalRow, 2, safeLong(report.getTotalOrders()), styles.totalInteger);
        writeNumber(totalRow, 3, safeLong(report.getTotalProductsSold()), styles.totalInteger);
        writeNumber(totalRow, 4, safeDouble(report.getTotalRevenue()), styles.totalCurrency);
        return rowIndex;
    }

    private int writeOrderDetail(XSSFSheet sheet, OrderReportResponse report, AdminReportFilterRequest filters,
                                 Styles styles) {
        int rowIndex = writeDetailHeader(sheet, styles, ReportFormatUtils.reportTitle("orders"), filters, 6);
        writeTableHeader(sheet, rowIndex++, styles, "STT", "Mã đơn hàng", "Khách hàng", "Ngày đặt", "Tổng tiền",
                "Trạng thái", "Phương thức thanh toán");
        int index = 1;
        for (OrderReportRowResponse row : safeList(report.getRows())) {
            Row excelRow = sheet.createRow(rowIndex++);
            boolean even = index % 2 == 0;
            writeNumber(excelRow, 0, index, even ? styles.altCenter : styles.center);
            writeText(excelRow, 1, "#" + safeLong(row.getOrderId()), even ? styles.altCenter : styles.center);
            writeText(excelRow, 2, row.getCustomerName(), even ? styles.altCell : styles.cell);
            writeText(excelRow, 3, ReportFormatUtils.formatDate(row.getCreatedAt()), even ? styles.altCenter : styles.center);
            writeNumber(excelRow, 4, safeDouble(row.getTotalAmount()), even ? styles.altCurrency : styles.currency);
            writeText(excelRow, 5, row.getStatusLabel(), even ? styles.altCenter : styles.center);
            writeText(excelRow, 6, row.getPaymentMethod(), even ? styles.altCell : styles.cell);
            index++;
        }
        Row totalRow = sheet.createRow(rowIndex++);
        writeText(totalRow, 0, "Tổng cộng", styles.total);
        merge(sheet, rowIndex - 1, rowIndex - 1, 0, 3);
        writeNumber(totalRow, 4, safeDouble(report.getTotalAmount()), styles.totalCurrency);
        writeText(totalRow, 5, ReportFormatUtils.formatNumber(report.getTotalOrders()) + " đơn hàng", styles.total);
        return rowIndex;
    }

    private int writeProductDetail(XSSFSheet sheet, ProductSalesReportResponse report,
                                   AdminReportFilterRequest filters, Styles styles) {
        int rowIndex = writeDetailHeader(sheet, styles, ReportFormatUtils.reportTitle("products"), filters, 5);
        writeTableHeader(sheet, rowIndex++, styles, "STT", "Mã sản phẩm", "Tên sản phẩm", "Danh mục",
                "Số lượng đã bán", "Doanh thu");
        int index = 1;
        for (ProductSalesReportRowResponse row : safeList(report.getRows())) {
            Row excelRow = sheet.createRow(rowIndex++);
            boolean even = index % 2 == 0;
            writeNumber(excelRow, 0, index, even ? styles.altCenter : styles.center);
            writeText(excelRow, 1, "#" + safeLong(row.getProductId()), even ? styles.altCenter : styles.center);
            writeText(excelRow, 2, row.getProductName(), even ? styles.altCell : styles.cell);
            writeText(excelRow, 3, row.getCategory(), even ? styles.altCenter : styles.center);
            writeNumber(excelRow, 4, safeLong(row.getQuantitySold()), even ? styles.altInteger : styles.integer);
            writeNumber(excelRow, 5, safeDouble(row.getRevenue()), even ? styles.altCurrency : styles.currency);
            index++;
        }
        Row totalRow = sheet.createRow(rowIndex++);
        writeText(totalRow, 0, "Tổng cộng", styles.total);
        merge(sheet, rowIndex - 1, rowIndex - 1, 0, 3);
        writeNumber(totalRow, 4, safeLong(report.getTotalQuantitySold()), styles.totalInteger);
        writeNumber(totalRow, 5, safeDouble(report.getTotalRevenue()), styles.totalCurrency);
        return rowIndex;
    }

    private int writeInventoryDetail(XSSFSheet sheet, InventoryReportResponse report,
                                     AdminReportFilterRequest filters, Styles styles) {
        int rowIndex = writeDetailHeader(sheet, styles, ReportFormatUtils.reportTitle("inventory"), filters, 6);
        writeTableHeader(sheet, rowIndex++, styles, "STT", "Mã sản phẩm", "Tên sản phẩm", "Danh mục",
                "Số lượng tồn", "Giá bán", "Trạng thái tồn kho");
        int index = 1;
        for (InventoryReportRowResponse row : safeList(report.getRows())) {
            Row excelRow = sheet.createRow(rowIndex++);
            boolean even = index % 2 == 0;
            writeNumber(excelRow, 0, index, even ? styles.altCenter : styles.center);
            writeText(excelRow, 1, "#" + safeLong(row.getProductId()), even ? styles.altCenter : styles.center);
            writeText(excelRow, 2, row.getProductName(), even ? styles.altCell : styles.cell);
            writeText(excelRow, 3, row.getCategory(), even ? styles.altCenter : styles.center);
            writeNumber(excelRow, 4, safeLong(row.getQuantity()), even ? styles.altInteger : styles.integer);
            writeNumber(excelRow, 5, safeDouble(row.getPrice()), even ? styles.altCurrency : styles.currency);
            writeText(excelRow, 6, row.getStockStatus(), even ? styles.altCenter : styles.center);
            index++;
        }
        Row totalRow = sheet.createRow(rowIndex++);
        writeText(totalRow, 0, "Tổng cộng", styles.total);
        merge(sheet, rowIndex - 1, rowIndex - 1, 0, 3);
        writeNumber(totalRow, 4, safeLong(report.getTotalProducts()), styles.totalInteger);
        writeNumber(totalRow, 5, safeDouble(report.getInventoryValue()), styles.totalCurrency);
        return rowIndex;
    }

    private int writeDetailHeader(XSSFSheet sheet, Styles styles, String title, AdminReportFilterRequest filters,
                                  int lastColumn) {
        sheet.setDisplayGridlines(false);
        Row brandRow = sheet.createRow(0);
        writeText(brandRow, 0, "ELECTRO", styles.brand);
        merge(sheet, 0, 0, 0, lastColumn);

        Row titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(28);
        writeText(titleRow, 0, uppercaseTitle(title), styles.title);
        merge(sheet, 1, 1, 0, lastColumn);

        writeMeta(sheet, styles, 3, "Khoảng thời gian lọc", buildPeriod(filters));
        writeMeta(sheet, styles, 4, "Từ ngày", buildFromDate(filters));
        writeMeta(sheet, styles, 5, "Đến ngày", buildToDate(filters));
        writeMeta(sheet, styles, 6, "Ngày xuất báo cáo", ReportFormatUtils.formatDateTime(LocalDateTime.now()));
        writeMeta(sheet, styles, 7, "Người lập báo cáo", ReportFormatUtils.exportedBy(filters.getExportedBy()));
        return 9;
    }

    private int writeRevenueChartData(XSSFSheet sheet, RevenueReportResponse report, Styles styles, int rowIndex, int col) {
        writeTableHeader(sheet, rowIndex, styles, "Thời gian", "Doanh thu");
        int currentRow = rowIndex + 1;
        for (RevenueReportRowResponse row : safeList(report.getRows())) {
            Row excelRow = sheet.createRow(currentRow++);
            writeText(excelRow, col, ReportFormatUtils.formatDate(row.getReportDate()), styles.cell);
            writeNumber(excelRow, col + 1, safeDouble(row.getRevenue()), styles.currency);
        }
        return currentRow - 1;
    }

    private int writeOrderStatusChartData(XSSFSheet sheet, OrderReportResponse report, Styles styles, int rowIndex, int col) {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (OrderReportRowResponse row : safeList(report.getRows())) {
            String status = row.getStatusLabel() == null ? "Không xác định" : row.getStatusLabel();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
        }
        writeTableHeader(sheet, rowIndex, styles, "Trạng thái", "Số đơn hàng");
        int currentRow = rowIndex + 1;
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            Row excelRow = sheet.createRow(currentRow++);
            writeText(excelRow, col, entry.getKey(), styles.cell);
            writeNumber(excelRow, col + 1, entry.getValue(), styles.integer);
        }
        return currentRow - 1;
    }

    private int writeProductChartData(XSSFSheet sheet, ProductSalesReportResponse report, Styles styles, int rowIndex, int col) {
        writeTableHeader(sheet, rowIndex, styles, "Sản phẩm", "Số lượng đã bán");
        int currentRow = rowIndex + 1;
        for (ProductSalesReportRowResponse row : safeList(report.getRows())) {
            Row excelRow = sheet.createRow(currentRow++);
            writeText(excelRow, col, shorten(row.getProductName(), 42), styles.cell);
            writeNumber(excelRow, col + 1, safeLong(row.getQuantitySold()), styles.integer);
        }
        return currentRow - 1;
    }

    private int writeInventoryChartData(XSSFSheet sheet, InventoryReportResponse report, Styles styles, int rowIndex, int col) {
        writeTableHeader(sheet, rowIndex, styles, "Trạng thái tồn kho", "Số sản phẩm");
        int currentRow = rowIndex + 1;
        Row inStock = sheet.createRow(currentRow++);
        writeText(inStock, col, "Còn hàng", styles.cell);
        writeNumber(inStock, col + 1, safeLong(report.getInStockProducts()), styles.integer);
        Row lowStock = sheet.createRow(currentRow++);
        writeText(lowStock, col, "Sắp hết hàng", styles.cell);
        writeNumber(lowStock, col + 1, safeLong(report.getLowStockProducts()), styles.integer);
        Row outOfStock = sheet.createRow(currentRow++);
        writeText(outOfStock, col, "Hết hàng", styles.cell);
        writeNumber(outOfStock, col + 1, safeLong(report.getOutOfStockProducts()), styles.integer);
        return currentRow - 1;
    }

    private void createBarChart(XSSFSheet sheet, String title, int categoryCol, int valueCol, int firstDataRow,
                                int lastDataRow, int col1, int row1, int col2, int row2, BarDirection direction) {
        if (lastDataRow < firstDataRow) {
            writeNoChartDataMessage(sheet, row1, col1);
            return;
        }

        XSSFChart chart = createChartFrame(sheet, title, col1, row1, col2, row2);
        XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
        valueAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        valueAxis.setNumberFormat("#,##0");

        XDDFCategoryDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(firstDataRow, lastDataRow, categoryCol, categoryCol));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(firstDataRow, lastDataRow, valueCol, valueCol));
        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, categoryAxis, valueAxis);
        data.setBarDirection(direction);
        XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(categories, values);
        series.setTitle(title, null);
        styleSeries(series, BRAND_ORANGE);
        applyDataPointColors(series, lastDataRow - firstDataRow + 1);
        chart.plot(data);
    }

    private void createLineChart(XSSFSheet sheet, String title, int categoryCol, int valueCol, int firstDataRow,
                                 int lastDataRow, int col1, int row1, int col2, int row2) {
        if (lastDataRow < firstDataRow) {
            writeNoChartDataMessage(sheet, row1, col1);
            return;
        }

        XSSFChart chart = createChartFrame(sheet, title, col1, row1, col2, row2);
        XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
        valueAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        valueAxis.setNumberFormat("#,##0");

        XDDFCategoryDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(firstDataRow, lastDataRow, categoryCol, categoryCol));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(firstDataRow, lastDataRow, valueCol, valueCol));
        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, categoryAxis, valueAxis);
        data.setGrouping(Grouping.STANDARD);
        XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(categories, values);
        series.setTitle(title, null);
        series.setLineProperties(lineProperties(BRAND_RED, 2.5));
        series.setFillProperties(solidFill(BRAND_RED));
        chart.plot(data);
    }

    private void createPieChart(XSSFSheet sheet, String title, int categoryCol, int valueCol, int firstDataRow,
                                int lastDataRow, int col1, int row1, int col2, int row2) {
        if (lastDataRow < firstDataRow) {
            writeNoChartDataMessage(sheet, row1, col1);
            return;
        }

        XSSFChart chart = createChartFrame(sheet, title, col1, row1, col2, row2);
        XDDFCategoryDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(firstDataRow, lastDataRow, categoryCol, categoryCol));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(firstDataRow, lastDataRow, valueCol, valueCol));
        XDDFPieChartData data = (XDDFPieChartData) chart.createData(ChartTypes.PIE, null, null);
        data.setVaryColors(true);
        XDDFChartData.Series series = data.addSeries(categories, values);
        series.setTitle(title, null);
        applyDataPointColors(series, lastDataRow - firstDataRow + 1);
        chart.plot(data);
    }

    private XSSFChart createChartFrame(XSSFSheet sheet, String title, int col1, int row1, int col2, int row2) {
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFChart chart = drawing.createChart(drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2));
        chart.setTitleText(title);
        chart.setTitleOverlay(false);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        chart.getOrAddShapeProperties().setFillProperties(solidFill("FFFFFF"));
        chart.getOrAddShapeProperties().setLineProperties(lineProperties(BRAND_BORDER, 0.75));
        return chart;
    }

    private void styleSeries(XDDFChartData.Series series, String colorHex) {
        series.setFillProperties(solidFill(colorHex));
        series.setLineProperties(lineProperties(colorHex, 1.25));
    }

    private void applyDataPointColors(XDDFChartData.Series series, int pointCount) {
        for (int i = 0; i < pointCount; i++) {
            String color = CHART_COLORS[i % CHART_COLORS.length];
            series.getDataPoint(i).setFillProperties(solidFill(color));
            series.getDataPoint(i).setLineProperties(lineProperties("FFFFFF", 0.75));
        }
    }

    private XDDFSolidFillProperties solidFill(String hex) {
        return new XDDFSolidFillProperties(XDDFColor.from(rgb(hex)));
    }

    private XDDFLineProperties lineProperties(String hex, double width) {
        XDDFLineProperties line = new XDDFLineProperties(solidFill(hex));
        line.setWidth(width);
        return line;
    }

    private byte[] rgb(String hex) {
        String value = hex.startsWith("#") ? hex.substring(1) : hex;
        return new byte[]{
                (byte) Integer.parseInt(value.substring(0, 2), 16),
                (byte) Integer.parseInt(value.substring(2, 4), 16),
                (byte) Integer.parseInt(value.substring(4, 6), 16)
        };
    }

    private void writeNoChartDataMessage(XSSFSheet sheet, int rowIndex, int columnIndex) {
        Row messageRow = getOrCreateRow(sheet, rowIndex);
        writeText(messageRow, columnIndex, "Không có dữ liệu phù hợp để tạo biểu đồ", nullSafeStyle(sheet));
    }

    private void writeFooter(XSSFSheet sheet, Styles styles, int startRow, String summary,
                             AdminReportFilterRequest filters, int lastColumn) {
        int rowIndex = startRow + 2;
        Row summaryRow = sheet.createRow(rowIndex++);
        writeText(summaryRow, 0, "Tổng kết", styles.sectionTitle);
        merge(sheet, rowIndex - 1, rowIndex - 1, 0, lastColumn);

        Row contentRow = sheet.createRow(rowIndex++);
        writeText(contentRow, 0, summary, styles.cell);
        merge(sheet, rowIndex - 1, rowIndex - 1, 0, lastColumn);

        Row noteRow = sheet.createRow(rowIndex++);
        writeText(noteRow, 0, AUTO_NOTE, styles.note);
        merge(sheet, rowIndex - 1, rowIndex - 1, 0, lastColumn);

        rowIndex += 2;
        Row signatureTitle = sheet.createRow(rowIndex++);
        writeText(signatureTitle, 0, "Người lập báo cáo", styles.signature);
        writeText(signatureTitle, Math.max(lastColumn - 1, 1), "Quản trị viên", styles.signature);

        rowIndex += 3;
        Row signatureName = sheet.createRow(rowIndex);
        writeText(signatureName, 0, ReportFormatUtils.exportedBy(filters.getExportedBy()), styles.signatureName);
        writeText(signatureName, Math.max(lastColumn - 1, 1), ReportFormatUtils.exportedBy(filters.getExportedBy()),
                styles.signatureName);
    }

    private void writeTableHeader(XSSFSheet sheet, int rowIndex, Styles styles, String... headers) {
        Row row = getOrCreateRow(sheet, rowIndex);
        row.setHeightInPoints(24);
        for (int i = 0; i < headers.length; i++) {
            writeText(row, i, headers[i], styles.header);
        }
        sheet.setAutoFilter(new CellRangeAddress(rowIndex, rowIndex, 0, headers.length - 1));
    }

    private void writeMeta(XSSFSheet sheet, Styles styles, int rowIndex, String label, String value) {
        Row row = getOrCreateRow(sheet, rowIndex);
        writeText(row, 0, label, styles.metaLabel);
        writeText(row, 1, value, styles.metaValue);
        merge(sheet, rowIndex, rowIndex, 1, 10);
    }

    private String buildPeriod(AdminReportFilterRequest filters) {
        if (filters == null || "inventory".equals(ReportFormatUtils.normalizeReportType(filters.getReportType()))) {
            return "Dữ liệu tồn kho hiện tại";
        }
        return ReportFormatUtils.formatDate(filters.getFromDate()) + " - " + ReportFormatUtils.formatDate(filters.getToDate());
    }

    private String buildFromDate(AdminReportFilterRequest filters) {
        if (filters == null || "inventory".equals(ReportFormatUtils.normalizeReportType(filters.getReportType()))) {
            return "-";
        }
        return ReportFormatUtils.formatDate(filters.getFromDate());
    }

    private String buildToDate(AdminReportFilterRequest filters) {
        if (filters == null || "inventory".equals(ReportFormatUtils.normalizeReportType(filters.getReportType()))) {
            return "-";
        }
        return ReportFormatUtils.formatDate(filters.getToDate());
    }

    private String buildStatusNote(AdminReportFilterRequest filters) {
        if (filters == null || "inventory".equals(ReportFormatUtils.normalizeReportType(filters.getReportType()))) {
            return "Trạng thái tồn kho được xác định theo số lượng hiện có của sản phẩm.";
        }
        return "Trạng thái đơn hàng: " + ReportFormatUtils.statusLabel(filters.getStatus())
                + ". Nếu chọn Tất cả, báo cáo tổng hợp mọi trạng thái đơn hàng.";
    }

    private void writeText(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex, CellType.STRING);
        cell.setCellValue(value == null || value.isBlank() ? "-" : value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private void writeNumber(Row row, int columnIndex, long value, CellStyle style) {
        Cell cell = row.createCell(columnIndex, CellType.NUMERIC);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void writeNumber(Row row, int columnIndex, double value, CellStyle style) {
        Cell cell = row.createCell(columnIndex, CellType.NUMERIC);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void finishSheet(XSSFSheet sheet, int columnCount) {
        sheet.createFreezePane(0, 8);
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.min(Math.max(sheet.getColumnWidth(i), 13 * 256), 48 * 256));
        }
    }

    private void setWidths(XSSFSheet sheet, int... widths) {
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    private void merge(XSSFSheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        if (lastCol >= firstCol) {
            sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
        }
    }

    private Row getOrCreateRow(XSSFSheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        return row == null ? sheet.createRow(rowIndex) : row;
    }

    private String shorten(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(maxLength - 3, 1)) + "...";
    }

    private String uppercaseTitle(String title) {
        return title == null ? "" : title.toUpperCase(VIETNAMESE);
    }

    private CellStyle nullSafeStyle(XSSFSheet sheet) {
        return sheet.getWorkbook().createCellStyle();
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0D : value;
    }

    private record Kpi(String label, String value) {
    }

    private static class Styles {
        final CellStyle brand;
        final CellStyle title;
        final CellStyle metaLabel;
        final CellStyle metaValue;
        final CellStyle kpiLabel;
        final CellStyle kpiValue;
        final CellStyle sectionTitle;
        final CellStyle header;
        final CellStyle cell;
        final CellStyle center;
        final CellStyle integer;
        final CellStyle currency;
        final CellStyle altCell;
        final CellStyle altCenter;
        final CellStyle altInteger;
        final CellStyle altCurrency;
        final CellStyle total;
        final CellStyle totalInteger;
        final CellStyle totalCurrency;
        final CellStyle note;
        final CellStyle signature;
        final CellStyle signatureName;

        Styles(XSSFWorkbook workbook) {
            Font brandFont = font(workbook, true, 12, "FFFFFF");
            Font titleFont = font(workbook, true, 17, "FFFFFF");
            Font headerFont = font(workbook, true, 10, "FFFFFF");
            Font normalFont = font(workbook, false, 10, BRAND_TEXT);
            Font boldFont = font(workbook, true, 10, BRAND_TEXT);
            Font kpiFont = font(workbook, true, 13, BRAND_DARK);
            Font noteFont = font(workbook, false, 10, BRAND_MUTED);

            brand = base(workbook, brandFont, BRAND_DARK, HorizontalAlignment.LEFT, false, false);
            title = base(workbook, titleFont, BRAND_RED, HorizontalAlignment.CENTER, false, false);
            metaLabel = base(workbook, boldFont, BRAND_SUBTLE, HorizontalAlignment.LEFT, false, true);
            metaValue = base(workbook, normalFont, "FFFFFF", HorizontalAlignment.LEFT, true, true);
            kpiLabel = base(workbook, boldFont, BRAND_SUBTLE, HorizontalAlignment.CENTER, true, true);
            kpiValue = base(workbook, kpiFont, "FFFFFF", HorizontalAlignment.CENTER, true, true);
            sectionTitle = base(workbook, boldFont, BRAND_SUBTLE, HorizontalAlignment.LEFT, true, true);
            header = base(workbook, headerFont, BRAND_DARK, HorizontalAlignment.CENTER, true, true);
            cell = base(workbook, normalFont, "FFFFFF", HorizontalAlignment.LEFT, true, true);
            center = base(workbook, normalFont, "FFFFFF", HorizontalAlignment.CENTER, true, true);
            integer = base(workbook, normalFont, "FFFFFF", HorizontalAlignment.RIGHT, false, true);
            integer.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            currency = base(workbook, normalFont, "FFFFFF", HorizontalAlignment.RIGHT, false, true);
            currency.setDataFormat(workbook.createDataFormat().getFormat("#,##0 [$₫-vi-VN]"));
            altCell = base(workbook, normalFont, BRAND_LIGHT, HorizontalAlignment.LEFT, true, true);
            altCenter = base(workbook, normalFont, BRAND_LIGHT, HorizontalAlignment.CENTER, true, true);
            altInteger = base(workbook, normalFont, BRAND_LIGHT, HorizontalAlignment.RIGHT, false, true);
            altInteger.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            altCurrency = base(workbook, normalFont, BRAND_LIGHT, HorizontalAlignment.RIGHT, false, true);
            altCurrency.setDataFormat(workbook.createDataFormat().getFormat("#,##0 [$₫-vi-VN]"));
            total = base(workbook, boldFont, BRAND_TOTAL, HorizontalAlignment.LEFT, true, true);
            totalInteger = base(workbook, boldFont, BRAND_TOTAL, HorizontalAlignment.RIGHT, false, true);
            totalInteger.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            totalCurrency = base(workbook, boldFont, BRAND_TOTAL, HorizontalAlignment.RIGHT, false, true);
            totalCurrency.setDataFormat(workbook.createDataFormat().getFormat("#,##0 [$₫-vi-VN]"));
            note = base(workbook, noteFont, "FFFFFF", HorizontalAlignment.LEFT, true, false);
            signature = base(workbook, boldFont, "FFFFFF", HorizontalAlignment.CENTER, false, false);
            signatureName = base(workbook, normalFont, "FFFFFF", HorizontalAlignment.CENTER, false, false);
        }

        private static Font font(XSSFWorkbook workbook, boolean bold, int size, String color) {
            XSSFFont font = workbook.createFont();
            font.setFontName("Arial");
            font.setBold(bold);
            font.setFontHeightInPoints((short) size);
            font.setColor(xssfColor(color));
            return font;
        }

        private static CellStyle base(XSSFWorkbook workbook, Font font, String fill, HorizontalAlignment alignment,
                                      boolean wrap, boolean border) {
            XSSFCellStyle style = workbook.createCellStyle();
            style.setFont(font);
            style.setAlignment(alignment);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setWrapText(wrap);
            if (fill != null) {
                style.setFillForegroundColor(xssfColor(fill));
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            if (border) {
                style.setBorderBottom(BorderStyle.THIN);
                style.setBorderTop(BorderStyle.THIN);
                style.setBorderLeft(BorderStyle.THIN);
                style.setBorderRight(BorderStyle.THIN);
                XSSFColor borderColor = xssfColor(BRAND_BORDER);
                style.setBottomBorderColor(borderColor);
                style.setTopBorderColor(borderColor);
                style.setLeftBorderColor(borderColor);
                style.setRightBorderColor(borderColor);
            }
            return style;
        }

        private static XSSFColor xssfColor(String hex) {
            String value = hex.startsWith("#") ? hex.substring(1) : hex;
            return new XSSFColor(new byte[]{
                    (byte) Integer.parseInt(value.substring(0, 2), 16),
                    (byte) Integer.parseInt(value.substring(2, 4), 16),
                    (byte) Integer.parseInt(value.substring(4, 6), 16)
            }, new DefaultIndexedColorMap());
        }
    }
}
