package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.request.AdminOrderSearchRequest;
import com.daizuongkk.web.dto.response.DashboardReportResponse;
import com.daizuongkk.web.dto.response.OrderStatusReportResponse;
import com.daizuongkk.web.dto.response.SalesTrendResponse;
import com.daizuongkk.web.dto.response.TopProductReportResponse;
import com.daizuongkk.web.model.Order;
import com.daizuongkk.web.model.OrderItem;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardReportExcelExporter {
	private static final String COMPANY_NAME = "ELECTRO";
	private static final String REPORT_NOTE = "Doanh thu hợp lệ tính theo đơn PAID, SHIPPED, COMPLETED.";
	private static final String[] VALID_STATUSES = { "PAID", "SHIPPED", "COMPLETED" };
	private static final String[] STATUS_ORDER = { "PENDING", "PAID", "SHIPPED", "COMPLETED", "CANCELLED" };
	private static final String[] ORDER_HEADERS = {
			"Mã đơn", "Ngày tạo", "Khách hàng", "Số điện thoại", "Địa chỉ", "Trạng thái", "Sản phẩm", "Số lượng",
			"Tổng tiền"
	};

	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private final DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public void exportDashboard(DashboardReportResponse report, OutputStream outputStream) {
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			WorkbookStyles styles = new WorkbookStyles(workbook);
			XSSFSheet overviewSheet = workbook.createSheet("Tong quan");
			XSSFSheet chartSheet = workbook.createSheet("Bieu do");
			XSSFSheet productSheet = workbook.createSheet("Top san pham");
			XSSFSheet orderSheet = workbook.createSheet("Don hang");
			XSSFSheet dataSheet = workbook.createSheet("Du lieu");

			writeDashboardOverview(overviewSheet, report, styles);
			writeDashboardChartData(dataSheet, report, styles);
			writeDashboardCharts(chartSheet, dataSheet, report, styles);
			writeTopProducts(productSheet, safeList(report.getTopProducts()), styles);
			writeOrders(orderSheet, safeList(report.getOrders()), styles);

			workbook.setSheetHidden(workbook.getSheetIndex(dataSheet), true);
			applyWorkbookMetadata(workbook, "Báo cáo kinh doanh Electro");
			workbook.write(outputStream);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot export dashboard report", e);
		}
	}

	public void exportOrders(List<Order> orders, AdminOrderSearchRequest filters, OutputStream outputStream) {
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			WorkbookStyles styles = new WorkbookStyles(workbook);
			XSSFSheet overviewSheet = workbook.createSheet("Tong quan");
			XSSFSheet orderSheet = workbook.createSheet("Don hang");
			XSSFSheet dataSheet = workbook.createSheet("Du lieu");
			List<Order> safeOrders = safeList(orders);
			OrderStats stats = summarizeOrders(safeOrders);

			writeOrderOverview(overviewSheet, dataSheet, safeOrders, stats, filters, styles);
			writeOrders(orderSheet, safeOrders, styles);

			workbook.setSheetHidden(workbook.getSheetIndex(dataSheet), true);
			applyWorkbookMetadata(workbook, "Báo cáo đơn hàng Electro");
			workbook.write(outputStream);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot export order report", e);
		}
	}

	private void writeDashboardOverview(XSSFSheet sheet, DashboardReportResponse report, WorkbookStyles styles) {
		prepareSheet(sheet, true);
		setWidths(sheet, 3, 20, 20, 20, 20, 20, 20, 20, 3);
		writeReportHeader(sheet, 1, "BÁO CÁO KINH DOANH", "Tổng quan kết quả bán hàng trong kỳ", styles);

		int row = 5;
		writeMetaRow(sheet, row++, "Kỳ báo cáo", formatDate(report.getFromDate()) + " - " + formatDate(report.getToDate()),
				styles);
		writeMetaRow(sheet, row++, "Ngày xuất", formatDateTime(report.getGeneratedAt()), styles);
		writeMetaRow(sheet, row++, "Ghi chú", REPORT_NOTE, styles);

		row += 2;
		writeKpiCard(sheet, row, 1, "Doanh thu hợp lệ", safeDouble(report.getValidRevenue()), styles.kpiCurrency,
				styles);
		writeKpiCard(sheet, row, 3, "Đơn hợp lệ", safeLong(report.getValidOrders()), styles.kpiNumber, styles);
		writeKpiCard(sheet, row, 5, "Giá trị TB/đơn", safeDouble(report.getAverageOrderValue()), styles.kpiCurrency,
				styles);
		writeKpiCard(sheet, row, 7, "Sản phẩm bán ra", safeLong(report.getItemsSold()), styles.kpiNumber, styles);

		row += 5;
		writeKpiCard(sheet, row, 1, "Tổng đơn", safeLong(report.getTotalOrders()), styles.kpiNumber, styles);
		writeKpiCard(sheet, row, 3, "Khách đã mua", safeLong(report.getUniqueCustomers()), styles.kpiNumber, styles);
		writeKpiCard(sheet, row, 5, "Đơn hoàn tất", safeLong(report.getCompletedOrders()), styles.kpiNumber, styles);
		writeKpiCard(sheet, row, 7, "Đơn đã hủy", safeLong(report.getCancelledOrders()), styles.kpiNumber, styles);

		row += 6;
		writeSectionTitle(sheet, row++, "Cơ cấu trạng thái đơn hàng", styles);
		writeHeader(sheet, row++, 1, styles, "Trạng thái", "Số đơn", "Doanh thu", "Tỷ trọng");
		int firstStatusRow = row;
		long totalOrders = safeLong(report.getTotalOrders());
		for (OrderStatusReportResponse item : safeList(report.getStatusBreakdown())) {
			Row excelRow = sheet.createRow(row++);
			text(excelRow, 1, statusLabel(item.getStatus()), styles.status(item.getStatus()));
			number(excelRow, 2, safeLong(item.getOrderCount()), styles.integer);
			number(excelRow, 3, safeDouble(item.getRevenue()), styles.currency);
			number(excelRow, 4, totalOrders > 0 ? safeLong(item.getOrderCount()) * 1D / totalOrders : 0D,
					styles.percent);
		}
		addFilter(sheet, firstStatusRow - 1, Math.max(firstStatusRow, row - 1), 1, 4);

		row += 2;
		writeSectionTitle(sheet, row++, "Hiệu suất trong kỳ", styles);
		writeHeader(sheet, row++, 1, styles, "Chỉ tiêu", "Giá trị", "Đơn vị");
		writeMetricRow(sheet, row++, "Tỷ lệ đơn hợp lệ",
				totalOrders > 0 ? safeLong(report.getValidOrders()) * 1D / totalOrders : 0D, "phần trăm", styles,
				styles.percent);
		writeMetricRow(sheet, row++, "Tỷ lệ hủy đơn",
				totalOrders > 0 ? safeLong(report.getCancelledOrders()) * 1D / totalOrders : 0D, "phần trăm", styles,
				styles.percent);
		writeMetricRow(sheet, row++, "Khách trung bình/ngày", averagePerDay(report.getUniqueCustomers(), report),
				"khách", styles, styles.decimal);
		writeMetricRow(sheet, row, "Doanh thu trung bình/ngày", averageRevenuePerDay(report), "VND", styles,
				styles.currency);

		freeze(sheet, 9);
	}

	private void writeDashboardChartData(XSSFSheet sheet, DashboardReportResponse report, WorkbookStyles styles) {
		prepareSheet(sheet, false);
		setWidths(sheet, 16, 16, 16, 5, 18, 16, 16, 5, 42, 16, 16);

		int trendHeader = 0;
		writeHeader(sheet, trendHeader, 0, styles, "Kỳ", "Doanh thu", "Số đơn");
		int row = trendHeader + 1;
		for (SalesTrendResponse item : safeList(report.getSalesTrend())) {
			Row excelRow = sheet.createRow(row++);
			text(excelRow, 0, item.getLabel(), styles.cell);
			number(excelRow, 1, safeDouble(item.getRevenue()), styles.currency);
			number(excelRow, 2, safeLong(item.getOrderCount()), styles.integer);
		}

		int statusHeader = 0;
		writeHeader(sheet, statusHeader, 4, styles, "Trạng thái", "Số đơn", "Doanh thu");
		row = statusHeader + 1;
		for (OrderStatusReportResponse item : safeList(report.getStatusBreakdown())) {
			Row excelRow = getOrCreateRow(sheet, row++);
			text(excelRow, 4, statusLabel(item.getStatus()), styles.cell);
			number(excelRow, 5, safeLong(item.getOrderCount()), styles.integer);
			number(excelRow, 6, safeDouble(item.getRevenue()), styles.currency);
		}

		int productHeader = 0;
		writeHeader(sheet, productHeader, 8, styles, "Sản phẩm", "Số lượng", "Doanh thu");
		row = productHeader + 1;
		for (TopProductReportResponse item : safeList(report.getTopProducts())) {
			Row excelRow = getOrCreateRow(sheet, row++);
			text(excelRow, 8, item.getProductName(), styles.cell);
			number(excelRow, 9, safeLong(item.getQuantitySold()), styles.integer);
			number(excelRow, 10, safeDouble(item.getRevenue()), styles.currency);
		}
	}

	private void writeDashboardCharts(XSSFSheet sheet, XSSFSheet dataSheet, DashboardReportResponse report,
			WorkbookStyles styles) {
		prepareSheet(sheet, true);
		setWidths(sheet, 3, 18, 18, 18, 18, 18, 18, 18, 18, 3);
		writeReportHeader(sheet, 1, "BIỂU ĐỒ PHÂN TÍCH", "Mỗi biểu đồ dùng một đơn vị đo riêng để tránh sai tỷ lệ",
				styles);

		boolean hasTrend = !safeList(report.getSalesTrend()).isEmpty();
		boolean hasStatus = !safeList(report.getStatusBreakdown()).isEmpty();
		boolean hasProducts = !safeList(report.getTopProducts()).isEmpty();

		if (hasTrend) {
			int trendLast = safeList(report.getSalesTrend()).size();
			createBarChart(sheet, dataSheet, "Doanh thu theo kỳ", 0, 1, 1, trendLast, 1, 5, 5, 20,
					BarDirection.COL);
			createBarChart(sheet, dataSheet, "Số đơn hợp lệ theo kỳ", 0, 2, 1, trendLast, 5, 5, 9, 20,
					BarDirection.COL);
		} else {
			writeEmptyChartMessage(sheet, 7, 1, "Chưa có dữ liệu xu hướng doanh thu trong kỳ.", styles);
		}

		if (hasStatus) {
			int statusLast = safeList(report.getStatusBreakdown()).size();
			createBarChart(sheet, dataSheet, "Số đơn theo trạng thái", 4, 5, 1, statusLast, 1, 22, 5, 37,
					BarDirection.COL);
		}

		if (hasProducts) {
			int productLast = safeList(report.getTopProducts()).size();
			createBarChart(sheet, dataSheet, "Top sản phẩm theo doanh thu", 8, 10, 1, productLast, 5, 22, 9, 37,
					BarDirection.BAR);
		} else {
			writeEmptyChartMessage(sheet, 25, 5, "Chưa có sản phẩm bán ra trong kỳ.", styles);
		}
	}

	private void writeTopProducts(XSSFSheet sheet, List<TopProductReportResponse> products, WorkbookStyles styles) {
		prepareSheet(sheet, true);
		setWidths(sheet, 10, 48, 16, 18, 14);
		writeReportHeader(sheet, 0, "TOP SẢN PHẨM BÁN CHẠY", "Xếp hạng theo doanh thu hợp lệ trong kỳ", styles);

		writeHeader(sheet, 4, 0, styles, "Hạng", "Sản phẩm", "Số lượng", "Doanh thu", "Tỷ trọng");
		double totalRevenue = products.stream().mapToDouble(item -> safeDouble(item.getRevenue())).sum();
		int row = 5;
		int rank = 1;
		for (TopProductReportResponse item : products) {
			Row excelRow = sheet.createRow(row);
			CellStyle rowStyle = row % 2 == 0 ? styles.zebra : styles.cell;
			text(excelRow, 0, String.valueOf(rank), rank <= 3 ? styles.rank(rank) : rowStyle);
			text(excelRow, 1, item.getProductName(), row % 2 == 0 ? styles.zebraWrap : styles.wrap);
			number(excelRow, 2, safeLong(item.getQuantitySold()), styles.integer);
			number(excelRow, 3, safeDouble(item.getRevenue()), styles.currency);
			number(excelRow, 4, totalRevenue > 0 ? safeDouble(item.getRevenue()) / totalRevenue : 0D,
					styles.percent);
			row++;
			rank++;
		}
		addFilter(sheet, 4, Math.max(4, row - 1), 0, 4);
		freeze(sheet, 5);
	}

	private void writeOrderOverview(XSSFSheet sheet, XSSFSheet dataSheet, List<Order> orders, OrderStats stats,
			AdminOrderSearchRequest filters, WorkbookStyles styles) {
		prepareSheet(sheet, true);
		prepareSheet(dataSheet, false);
		setWidths(sheet, 3, 20, 20, 20, 20, 20, 20, 3);
		setWidths(dataSheet, 18, 16, 18);
		writeReportHeader(sheet, 1, "BÁO CÁO ĐƠN HÀNG", "Tổng hợp danh sách đơn hàng theo bộ lọc", styles);

		int row = 5;
		writeMetaRow(sheet, row++, "Ngày xuất", dateTimeFormatter.format(LocalDateTime.now()), styles);
		writeMetaRow(sheet, row++, "Từ khóa", valueOrAll(filters == null ? null : filters.getKeyword()), styles);
		writeMetaRow(sheet, row++, "Trạng thái", statusLabel(valueOrAllStatus(filters == null ? null : filters.getStatus())), styles);
		writeMetaRow(sheet, row++, "Khoảng ngày", formatFilterDateRange(filters), styles);
		writeMetaRow(sheet, row++, "Khoảng tổng tiền", formatMoneyRange(filters), styles);

		row += 2;
		writeKpiCard(sheet, row, 1, "Tổng đơn xuất", stats.totalOrders, styles.kpiNumber, styles);
		writeKpiCard(sheet, row, 3, "Tổng giá trị", stats.totalRevenue, styles.kpiCurrency, styles);
		writeKpiCard(sheet, row, 5, "Sản phẩm", stats.itemsSold, styles.kpiNumber, styles);

		row += 5;
		writeKpiCard(sheet, row, 1, "Đơn hợp lệ", stats.validOrders, styles.kpiNumber, styles);
		writeKpiCard(sheet, row, 3, "Doanh thu hợp lệ", stats.validRevenue, styles.kpiCurrency, styles);
		writeKpiCard(sheet, row, 5, "Giá trị TB/đơn", stats.averageOrderValue(), styles.kpiCurrency, styles);

		row += 6;
		writeSectionTitle(sheet, row++, "Thống kê theo trạng thái", styles);
		writeHeader(sheet, row++, 1, styles, "Trạng thái", "Số đơn", "Giá trị", "Tỷ trọng");
		int firstStatusRow = row;
		for (Map.Entry<String, StatusStats> entry : stats.byStatus.entrySet()) {
			Row excelRow = sheet.createRow(row++);
			text(excelRow, 1, statusLabel(entry.getKey()), styles.status(entry.getKey()));
			number(excelRow, 2, entry.getValue().orderCount, styles.integer);
			number(excelRow, 3, entry.getValue().revenue, styles.currency);
			number(excelRow, 4, stats.totalOrders > 0 ? entry.getValue().orderCount * 1D / stats.totalOrders : 0D,
					styles.percent);
		}
		addFilter(sheet, firstStatusRow - 1, Math.max(firstStatusRow, row - 1), 1, 4);

		writeOrderChartData(dataSheet, stats, styles);
		if (!orders.isEmpty()) {
			createBarChart(sheet, dataSheet, "Số đơn theo trạng thái", 0, 1, 1, stats.byStatus.size(), 1, row + 2, 6,
					row + 18, BarDirection.COL);
		}
		freeze(sheet, 11);
	}

	private void writeOrderChartData(XSSFSheet sheet, OrderStats stats, WorkbookStyles styles) {
		writeHeader(sheet, 0, 0, styles, "Trạng thái", "Số đơn", "Giá trị");
		int row = 1;
		for (Map.Entry<String, StatusStats> entry : stats.byStatus.entrySet()) {
			Row excelRow = sheet.createRow(row++);
			text(excelRow, 0, statusLabel(entry.getKey()), styles.cell);
			number(excelRow, 1, entry.getValue().orderCount, styles.integer);
			number(excelRow, 2, entry.getValue().revenue, styles.currency);
		}
	}

	private void writeOrders(XSSFSheet sheet, List<Order> orders, WorkbookStyles styles) {
		prepareSheet(sheet, true);
		setWidths(sheet, 12, 19, 24, 16, 42, 16, 58, 13, 18);
		writeReportHeader(sheet, 0, "DANH SÁCH ĐƠN HÀNG", "Chi tiết từng đơn hàng và sản phẩm", styles);
		writeHeader(sheet, 4, 0, styles, ORDER_HEADERS);

		int row = 5;
		for (Order order : orders) {
			Row excelRow = sheet.createRow(row);
			excelRow.setHeightInPoints(Math.max(30, Math.min(78, 24 + itemLineCount(order) * 12)));
			CellStyle base = row % 2 == 0 ? styles.zebra : styles.cell;
			CellStyle wrap = row % 2 == 0 ? styles.zebraWrap : styles.wrap;
			text(excelRow, 0, "#" + safeLong(order.getId()), base);
			text(excelRow, 1, formatDate(order.getCreatedAt()), base);
			text(excelRow, 2, order.getRecipientName(), base);
			text(excelRow, 3, order.getPhone(), base);
			text(excelRow, 4, order.getAddress(), wrap);
			text(excelRow, 5, statusLabel(order.getStatus()), styles.status(order.getStatus()));
			text(excelRow, 6, summarizeItems(order), wrap);
			number(excelRow, 7, totalQuantity(order), styles.integer);
			number(excelRow, 8, safeDouble(order.getTotalPrice()), styles.currency);
			row++;
		}
		addFilter(sheet, 4, Math.max(4, row - 1), 0, ORDER_HEADERS.length - 1);
		freeze(sheet, 5);
	}

	private void createBarChart(XSSFSheet chartSheet, XSSFSheet dataSheet, String title, int categoryCol, int valueCol,
			int firstDataRow, int lastDataRow, int col1, int row1, int col2, int row2, BarDirection direction) {
		if (lastDataRow < firstDataRow) {
			return;
		}

		XSSFDrawing drawing = chartSheet.createDrawingPatriarch();
		XSSFChart chart = drawing.createChart(drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2));
		chart.setTitleText(title);
		chart.setTitleOverlay(false);
		XDDFChartLegend legend = chart.getOrAddLegend();
		legend.setPosition(LegendPosition.BOTTOM);

		XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
		XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
		valueAxis.setCrosses(AxisCrosses.AUTO_ZERO);

		XDDFCategoryDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(dataSheet,
				new CellRangeAddress(firstDataRow, lastDataRow, categoryCol, categoryCol));
		XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(dataSheet,
				new CellRangeAddress(firstDataRow, lastDataRow, valueCol, valueCol));
		XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, categoryAxis, valueAxis);
		data.setBarDirection(direction);
		XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(categories, values);
		Cell header = dataSheet.getRow(firstDataRow - 1).getCell(valueCol);
		series.setTitle(header == null ? "Giá trị" : header.getStringCellValue(), null);
		chart.plot(data);
	}

	private void writeReportHeader(Sheet sheet, int rowIndex, String title, String subtitle, WorkbookStyles styles) {
		Row brandRow = getOrCreateRow(sheet, rowIndex);
		brandRow.setHeightInPoints(22);
		text(brandRow, 1, COMPANY_NAME, styles.brand);
		merge(sheet, rowIndex, rowIndex, 1, 7);

		Row titleRow = getOrCreateRow(sheet, rowIndex + 1);
		titleRow.setHeightInPoints(32);
		text(titleRow, 1, title, styles.title);
		merge(sheet, rowIndex + 1, rowIndex + 1, 1, 7);

		Row subtitleRow = getOrCreateRow(sheet, rowIndex + 2);
		subtitleRow.setHeightInPoints(22);
		text(subtitleRow, 1, subtitle, styles.subtitle);
		merge(sheet, rowIndex + 2, rowIndex + 2, 1, 7);
	}

	private void writeMetaRow(Sheet sheet, int rowIndex, String label, String value, WorkbookStyles styles) {
		Row row = getOrCreateRow(sheet, rowIndex);
		text(row, 1, label, styles.metaLabel);
		text(row, 2, value, styles.metaValue);
		merge(sheet, rowIndex, rowIndex, 2, 7);
	}

	private void writeKpiCard(Sheet sheet, int rowIndex, int colIndex, String label, Object value, CellStyle valueStyle,
			WorkbookStyles styles) {
		Row labelRow = getOrCreateRow(sheet, rowIndex);
		Row valueRow = getOrCreateRow(sheet, rowIndex + 1);
		Row noteRow = getOrCreateRow(sheet, rowIndex + 2);
		labelRow.setHeightInPoints(22);
		valueRow.setHeightInPoints(32);
		noteRow.setHeightInPoints(18);
		text(labelRow, colIndex, label, styles.kpiLabel);
		writeValue(valueRow, colIndex, value, valueStyle);
		text(noteRow, colIndex, "Kỳ báo cáo", styles.kpiNote);
		merge(sheet, rowIndex, rowIndex, colIndex, colIndex + 1);
		merge(sheet, rowIndex + 1, rowIndex + 1, colIndex, colIndex + 1);
		merge(sheet, rowIndex + 2, rowIndex + 2, colIndex, colIndex + 1);
	}

	private void writeSectionTitle(Sheet sheet, int rowIndex, String title, WorkbookStyles styles) {
		Row row = getOrCreateRow(sheet, rowIndex);
		row.setHeightInPoints(24);
		text(row, 1, title, styles.sectionTitle);
		merge(sheet, rowIndex, rowIndex, 1, 7);
	}

	private void writeHeader(Sheet sheet, int rowIndex, int startCol, WorkbookStyles styles, String... headers) {
		Row row = getOrCreateRow(sheet, rowIndex);
		row.setHeightInPoints(24);
		for (int i = 0; i < headers.length; i++) {
			text(row, startCol + i, headers[i], styles.tableHeader);
		}
	}

	private void writeMetricRow(Sheet sheet, int rowIndex, String label, Object value, String unit, WorkbookStyles styles,
			CellStyle valueStyle) {
		Row row = getOrCreateRow(sheet, rowIndex);
		text(row, 1, label, styles.cell);
		writeValue(row, 2, value, valueStyle);
		text(row, 3, unit, styles.cell);
	}

	private void writeEmptyChartMessage(Sheet sheet, int rowIndex, int colIndex, String message, WorkbookStyles styles) {
		Row row = getOrCreateRow(sheet, rowIndex);
		row.setHeightInPoints(28);
		text(row, colIndex, message, styles.emptyMessage);
		merge(sheet, rowIndex, rowIndex, colIndex, colIndex + 3);
	}

	private void writeValue(Row row, int col, Object value, CellStyle style) {
		if (value instanceof Number number) {
			number(row, col, number.doubleValue(), style);
			return;
		}
		text(row, col, value == null ? "-" : String.valueOf(value), style);
	}

	private void text(Row row, int col, String value, CellStyle style) {
		Cell cell = row.createCell(col, CellType.STRING);
		cell.setCellValue(value == null || value.isBlank() ? "-" : value);
		cell.setCellStyle(style);
	}

	private void number(Row row, int col, long value, CellStyle style) {
		Cell cell = row.createCell(col, CellType.NUMERIC);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}

	private void number(Row row, int col, double value, CellStyle style) {
		Cell cell = row.createCell(col, CellType.NUMERIC);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}

	private OrderStats summarizeOrders(List<Order> orders) {
		OrderStats stats = new OrderStats();
		for (String status : STATUS_ORDER) {
			stats.byStatus.put(status, new StatusStats());
		}

		for (Order order : orders) {
			double total = safeDouble(order.getTotalPrice());
			long quantity = totalQuantity(order);
			stats.totalOrders++;
			stats.totalRevenue += total;
			stats.itemsSold += quantity;
			if (isValidStatus(order.getStatus())) {
				stats.validOrders++;
				stats.validRevenue += total;
			}

			String status = normalizeStatus(order.getStatus());
			StatusStats statusStats = stats.byStatus.computeIfAbsent(status, key -> new StatusStats());
			statusStats.orderCount++;
			statusStats.revenue += total;
		}
		stats.byStatus.entrySet().removeIf(entry -> entry.getValue().orderCount == 0 && !isKnownStatus(entry.getKey()));
		return stats;
	}

	private boolean isValidStatus(String status) {
		String normalized = normalizeStatus(status);
		for (String validStatus : VALID_STATUSES) {
			if (validStatus.equals(normalized)) {
				return true;
			}
		}
		return false;
	}

	private boolean isKnownStatus(String status) {
		for (String knownStatus : STATUS_ORDER) {
			if (knownStatus.equals(status)) {
				return true;
			}
		}
		return false;
	}

	private String normalizeStatus(String status) {
		return status == null || status.isBlank() ? "UNKNOWN" : status.trim().toUpperCase();
	}

	private String summarizeItems(Order order) {
		if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
			return "-";
		}

		StringBuilder builder = new StringBuilder();
		for (OrderItem item : order.getItems()) {
			if (!builder.isEmpty()) {
				builder.append('\n');
			}
			builder.append(safeLong(item.getQuantity()))
					.append(" x ")
					.append(item.getProductName() == null ? "Sản phẩm" : item.getProductName());
		}
		return builder.toString();
	}

	private int itemLineCount(Order order) {
		return order == null || order.getItems() == null || order.getItems().isEmpty() ? 1 : order.getItems().size();
	}

	private long totalQuantity(Order order) {
		if (order == null || order.getItems() == null) {
			return 0L;
		}
		return order.getItems().stream().mapToLong(item -> safeLong(item.getQuantity())).sum();
	}

	private double averagePerDay(Long value, DashboardReportResponse report) {
		long days = reportDays(report);
		return days > 0 ? safeLong(value) * 1D / days : 0D;
	}

	private double averageRevenuePerDay(DashboardReportResponse report) {
		long days = reportDays(report);
		return days > 0 ? safeDouble(report.getValidRevenue()) / days : 0D;
	}

	private long reportDays(DashboardReportResponse report) {
		if (report == null || report.getFromDate() == null || report.getToDate() == null) {
			return 1L;
		}
		return Math.max(1L, java.time.temporal.ChronoUnit.DAYS.between(report.getFromDate(), report.getToDate()) + 1L);
	}

	private String formatDate(Date date) {
		if (date == null) {
			return "-";
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(dateTimeFormatter);
	}

	private String formatDate(java.time.LocalDate date) {
		return date == null ? "-" : date.format(dateOnlyFormatter);
	}

	private String formatDateTime(LocalDateTime dateTime) {
		return dateTime == null ? "-" : dateTime.format(dateTimeFormatter);
	}

	private String statusLabel(String status) {
		return switch (normalizeStatus(status)) {
			case "PENDING" -> "Chờ xử lý";
			case "PAID" -> "Đã thanh toán";
			case "SHIPPED" -> "Đang giao";
			case "COMPLETED" -> "Hoàn tất";
			case "CANCELLED" -> "Đã hủy";
			case "ALL" -> "Tất cả";
			default -> status == null || status.isBlank() ? "Không xác định" : status;
		};
	}

	private String valueOrAll(Object value) {
		return value == null || String.valueOf(value).isBlank() ? "Tất cả" : String.valueOf(value);
	}

	private String valueOrAllStatus(String status) {
		return status == null || status.isBlank() ? "ALL" : status;
	}

	private String formatFilterDateRange(AdminOrderSearchRequest filters) {
		if (filters == null) {
			return "Tất cả";
		}
		return valueOrAll(filters.getFromDate()) + " - " + valueOrAll(filters.getToDate());
	}

	private String formatMoneyRange(AdminOrderSearchRequest filters) {
		if (filters == null) {
			return "Tất cả";
		}
		return valueOrAll(filters.getMinTotal()) + " - " + valueOrAll(filters.getMaxTotal());
	}

	private long safeLong(Long value) {
		return value == null ? 0L : value;
	}

	private double safeDouble(Double value) {
		return value == null ? 0D : value;
	}

	private <T> List<T> safeList(List<T> values) {
		return values == null ? List.of() : values;
	}

	private void prepareSheet(Sheet sheet, boolean printable) {
		sheet.setDisplayGridlines(false);
		sheet.setAutobreaks(true);
		if (printable) {
			sheet.getPrintSetup().setLandscape(true);
			sheet.getPrintSetup().setFitWidth((short) 1);
			sheet.getPrintSetup().setFitHeight((short) 0);
		}
		Footer footer = sheet.getFooter();
		footer.setCenter("Electro - Báo cáo tạo tự động");
		footer.setRight("Trang &P / &N");
	}

	private void addFilter(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
		if (lastRow >= firstRow) {
			sheet.setAutoFilter(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
		}
	}

	private void freeze(Sheet sheet, int row) {
		sheet.createFreezePane(0, row);
	}

	private void merge(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
		sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
	}

	private void setWidths(Sheet sheet, int... widths) {
		for (int i = 0; i < widths.length; i++) {
			sheet.setColumnWidth(i, widths[i] * 256);
		}
	}

	private Row getOrCreateRow(Sheet sheet, int rowIndex) {
		Row row = sheet.getRow(rowIndex);
		return row == null ? sheet.createRow(rowIndex) : row;
	}

	private void applyWorkbookMetadata(XSSFWorkbook workbook, String title) {
		workbook.getProperties().getCoreProperties().setTitle(title);
		workbook.getProperties().getCoreProperties().setCreator("Electro Management System");
		workbook.getProperties().getCoreProperties().setDescription(REPORT_NOTE);
	}

	private static class OrderStats {
		long totalOrders;
		long validOrders;
		long itemsSold;
		double totalRevenue;
		double validRevenue;
		Map<String, StatusStats> byStatus = new LinkedHashMap<>();

		double averageOrderValue() {
			return validOrders > 0 ? validRevenue / validOrders : 0D;
		}
	}

	private static class StatusStats {
		long orderCount;
		double revenue;
	}

	private static class WorkbookStyles {
		final CellStyle brand;
		final CellStyle title;
		final CellStyle subtitle;
		final CellStyle sectionTitle;
		final CellStyle metaLabel;
		final CellStyle metaValue;
		final CellStyle tableHeader;
		final CellStyle cell;
		final CellStyle zebra;
		final CellStyle wrap;
		final CellStyle zebraWrap;
		final CellStyle integer;
		final CellStyle decimal;
		final CellStyle currency;
		final CellStyle percent;
		final CellStyle kpiLabel;
		final CellStyle kpiNumber;
		final CellStyle kpiCurrency;
		final CellStyle kpiNote;
		final CellStyle emptyMessage;
		final CellStyle pending;
		final CellStyle paid;
		final CellStyle shipped;
		final CellStyle completed;
		final CellStyle cancelled;
		final CellStyle neutralStatus;
		final CellStyle rank1;
		final CellStyle rank2;
		final CellStyle rank3;

		WorkbookStyles(XSSFWorkbook workbook) {
			CreationHelper helper = workbook.getCreationHelper();
			Font brandFont = font(workbook, true, 12, IndexedColors.WHITE);
			Font titleFont = font(workbook, true, 18, IndexedColors.WHITE);
			Font subtitleFont = font(workbook, false, 10, IndexedColors.GREY_80_PERCENT);
			Font sectionFont = font(workbook, true, 12, IndexedColors.WHITE);
			Font headerFont = font(workbook, true, 10, IndexedColors.WHITE);
			Font normalFont = font(workbook, false, 10, IndexedColors.BLACK);
			Font mutedFont = font(workbook, false, 9, IndexedColors.GREY_50_PERCENT);
			Font kpiValueFont = font(workbook, true, 16, IndexedColors.WHITE);
			Font statusFont = font(workbook, true, 10, IndexedColors.BLACK);
			Font rankFont = font(workbook, true, 10, IndexedColors.WHITE);

			brand = filled(workbook, brandFont, IndexedColors.DARK_BLUE, HorizontalAlignment.LEFT, false, false);
			title = filled(workbook, titleFont, IndexedColors.DARK_BLUE, HorizontalAlignment.CENTER, false, false);
			subtitle = filled(workbook, subtitleFont, IndexedColors.GREY_25_PERCENT, HorizontalAlignment.CENTER, false,
					false);
			sectionTitle = filled(workbook, sectionFont, IndexedColors.TEAL, HorizontalAlignment.LEFT, false, false);
			metaLabel = base(workbook, normalFont, IndexedColors.GREY_25_PERCENT, false, HorizontalAlignment.LEFT, true);
			metaValue = base(workbook, normalFont, null, true, HorizontalAlignment.LEFT, true);
			tableHeader = filled(workbook, headerFont, IndexedColors.DARK_BLUE, HorizontalAlignment.CENTER, false, true);
			cell = base(workbook, normalFont, null, false, HorizontalAlignment.LEFT, true);
			zebra = base(workbook, normalFont, IndexedColors.GREY_25_PERCENT, false, HorizontalAlignment.LEFT, true);
			wrap = base(workbook, normalFont, null, true, HorizontalAlignment.LEFT, true);
			zebraWrap = base(workbook, normalFont, IndexedColors.GREY_25_PERCENT, true, HorizontalAlignment.LEFT, true);
			integer = base(workbook, normalFont, null, false, HorizontalAlignment.RIGHT, true);
			integer.setDataFormat(helper.createDataFormat().getFormat("#,##0"));
			decimal = base(workbook, normalFont, null, false, HorizontalAlignment.RIGHT, true);
			decimal.setDataFormat(helper.createDataFormat().getFormat("#,##0.0"));
			currency = base(workbook, normalFont, null, false, HorizontalAlignment.RIGHT, true);
			currency.setDataFormat(helper.createDataFormat().getFormat("#,##0 [$₫-vi-VN]"));
			percent = base(workbook, normalFont, null, false, HorizontalAlignment.RIGHT, true);
			percent.setDataFormat(helper.createDataFormat().getFormat("0.0%"));
			kpiLabel = base(workbook, mutedFont, IndexedColors.GREY_25_PERCENT, false, HorizontalAlignment.CENTER, true);
			kpiNumber = filled(workbook, kpiValueFont, IndexedColors.DARK_BLUE, HorizontalAlignment.CENTER, false, true);
			kpiNumber.setDataFormat(helper.createDataFormat().getFormat("#,##0"));
			kpiCurrency = filled(workbook, kpiValueFont, IndexedColors.TEAL, HorizontalAlignment.CENTER, false, true);
			kpiCurrency.setDataFormat(helper.createDataFormat().getFormat("#,##0 [$₫-vi-VN]"));
			kpiNote = base(workbook, mutedFont, IndexedColors.GREY_25_PERCENT, false, HorizontalAlignment.CENTER, true);
			emptyMessage = base(workbook, normalFont, IndexedColors.LIGHT_YELLOW, true, HorizontalAlignment.CENTER, true);

			pending = base(workbook, statusFont, IndexedColors.LIGHT_YELLOW, false, HorizontalAlignment.CENTER, true);
			paid = base(workbook, statusFont, IndexedColors.LIGHT_GREEN, false, HorizontalAlignment.CENTER, true);
			shipped = base(workbook, statusFont, IndexedColors.LIGHT_TURQUOISE, false, HorizontalAlignment.CENTER, true);
			completed = base(workbook, statusFont, IndexedColors.BRIGHT_GREEN, false, HorizontalAlignment.CENTER, true);
			cancelled = base(workbook, statusFont, IndexedColors.ROSE, false, HorizontalAlignment.CENTER, true);
			neutralStatus = base(workbook, statusFont, IndexedColors.GREY_25_PERCENT, false, HorizontalAlignment.CENTER,
					true);

			rank1 = filled(workbook, rankFont, IndexedColors.ORANGE, HorizontalAlignment.CENTER, false, true);
			rank2 = filled(workbook, rankFont, IndexedColors.GREY_50_PERCENT, HorizontalAlignment.CENTER, false, true);
			rank3 = filled(workbook, rankFont, IndexedColors.TAN, HorizontalAlignment.CENTER, false, true);
		}

		CellStyle status(String status) {
			return switch (status == null ? "" : status.trim().toUpperCase()) {
				case "PENDING" -> pending;
				case "PAID" -> paid;
				case "SHIPPED" -> shipped;
				case "COMPLETED" -> completed;
				case "CANCELLED" -> cancelled;
				default -> neutralStatus;
			};
		}

		CellStyle rank(int rank) {
			return switch (rank) {
				case 1 -> rank1;
				case 2 -> rank2;
				case 3 -> rank3;
				default -> cell;
			};
		}

		private static Font font(XSSFWorkbook workbook, boolean bold, int size, IndexedColors color) {
			Font font = workbook.createFont();
			font.setFontName("Calibri");
			font.setBold(bold);
			font.setFontHeightInPoints((short) size);
			font.setColor(color.getIndex());
			return font;
		}

		private static CellStyle filled(XSSFWorkbook workbook, Font font, IndexedColors fill,
				HorizontalAlignment alignment, boolean wrap, boolean border) {
			return base(workbook, font, fill, wrap, alignment, border);
		}

		private static CellStyle base(XSSFWorkbook workbook, Font font, IndexedColors fill, boolean wrap,
				HorizontalAlignment alignment, boolean border) {
			CellStyle style = workbook.createCellStyle();
			style.setFont(font);
			style.setVerticalAlignment(VerticalAlignment.CENTER);
			style.setAlignment(alignment);
			style.setWrapText(wrap);
			if (fill != null) {
				style.setFillForegroundColor(fill.getIndex());
				style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			}
			if (border) {
				style.setBorderBottom(BorderStyle.THIN);
				style.setBorderTop(BorderStyle.THIN);
				style.setBorderLeft(BorderStyle.THIN);
				style.setBorderRight(BorderStyle.THIN);
				style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
				style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
				style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
				style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
			}
			return style;
		}
	}
}
