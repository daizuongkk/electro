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
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportPdfExporter {
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");
    private static final Color PRIMARY = hex("D10024");
    private static final Color SECONDARY = hex("E66239");
    private static final Color DARK = hex("302C4D");
    private static final Color TEXT = hex("2B2D42");
    private static final Color MUTED = hex("657E92");
    private static final Color LIGHT_GREY = hex("FBFBFC");
    private static final Color KPI_BACKGROUND = hex("FAE0D7");
    private static final Color BORDER = hex("E4E7ED");
    private static final Color TOTAL_BACKGROUND = hex("FFF4E8");
    private static final Color[] CHART_COLORS = {
            hex("D10024"), hex("E66239"), hex("302C4D"), hex("657E92"),
            hex("00C951"), hex("00B8DB"), hex("F0B100"), hex("FB2C36")
    };
    private static final String AUTO_NOTE = "Báo cáo được tạo tự động từ hệ thống Electro";
    private static final String REGULAR_FONT = "Roboto-Regular.ttf";
    private static final String SEMIBOLD_FONT = "Roboto-SemiBold.ttf";
    private static final String BOLD_FONT = "Roboto-Bold.ttf";

    private final BaseFont regularBaseFont;
    private final BaseFont semiboldBaseFont;
    private final BaseFont boldBaseFont;
    private final java.awt.Font chartFont;
    private final java.awt.Font chartBoldFont;

    public ReportPdfExporter(String fontPath) throws IOException {
        this(fontPath, null, null);
    }

    public ReportPdfExporter(String regularFontPath, String semiboldFontPath, String boldFontPath) throws IOException {
        this.regularBaseFont = loadBaseFont(regularFontPath, REGULAR_FONT);
        this.semiboldBaseFont = loadBaseFont(semiboldFontPath, SEMIBOLD_FONT);
        this.boldBaseFont = loadBaseFont(boldFontPath, BOLD_FONT);
        this.chartFont = loadChartFont(regularFontPath, REGULAR_FONT, java.awt.Font.PLAIN, 12);
        this.chartBoldFont = loadChartFont(boldFontPath, BOLD_FONT, java.awt.Font.BOLD, 14);
    }

    public void exportRevenue(RevenueReportResponse report, AdminReportFilterRequest filters, OutputStream outputStream)
            throws IOException {
        Document document = openDocument(outputStream);
        try {
            writeDocumentHeader(document, ReportFormatUtils.reportTitle("revenue"), filters);
            writeKpis(document,
                    new Kpi("Tổng doanh thu", ReportFormatUtils.formatCurrency(report.getTotalRevenue())),
                    new Kpi("Tổng số đơn hàng", ReportFormatUtils.formatNumber(report.getTotalOrders())),
                    new Kpi("Tổng số sản phẩm đã bán", ReportFormatUtils.formatNumber(report.getTotalProductsSold())),
                    new Kpi("Giá trị đơn hàng trung bình", ReportFormatUtils.formatCurrency(report.getAverageOrderValue())));
            writeChart(document, createRevenueChart(report));

            PdfPTable table = createTable(new float[]{0.55f, 1.2f, 1.2f, 1.5f, 1.6f});
            writeHeaders(table, "STT", "Ngày", "Số đơn hàng", "Số sản phẩm đã bán", "Doanh thu");
            int index = 1;
            for (RevenueReportRowResponse row : safeList(report.getRows())) {
                addCell(table, String.valueOf(index++), Element.ALIGN_CENTER);
                addCell(table, ReportFormatUtils.formatDate(row.getReportDate()), Element.ALIGN_CENTER);
                addCell(table, ReportFormatUtils.formatNumber(safeLong(row.getOrderCount())), Element.ALIGN_RIGHT);
                addCell(table, ReportFormatUtils.formatNumber(safeLong(row.getProductCount())), Element.ALIGN_RIGHT);
                addCell(table, ReportFormatUtils.formatCurrency(row.getRevenue()), Element.ALIGN_RIGHT);
            }
            addTotalRow(table, "Tổng cộng", "", ReportFormatUtils.formatNumber(report.getTotalOrders()),
                    ReportFormatUtils.formatNumber(report.getTotalProductsSold()),
                    ReportFormatUtils.formatCurrency(report.getTotalRevenue()));
            document.add(table);
            writeFooter(document, "Tổng doanh thu trong kỳ: " + ReportFormatUtils.formatCurrency(report.getTotalRevenue()),
                    filters);
        } catch (DocumentException e) {
            throw new IOException("Không thể xuất PDF báo cáo doanh thu", e);
        } finally {
            document.close();
        }
    }

    public void exportOrders(OrderReportResponse report, AdminReportFilterRequest filters, OutputStream outputStream)
            throws IOException {
        Document document = openDocument(outputStream);
        try {
            writeDocumentHeader(document, ReportFormatUtils.reportTitle("orders"), filters);
            writeKpis(document,
                    new Kpi("Tổng số đơn hàng", ReportFormatUtils.formatNumber(report.getTotalOrders())),
                    new Kpi("Tổng giá trị đơn hàng", ReportFormatUtils.formatCurrency(report.getTotalAmount())),
                    new Kpi("Trạng thái đang lọc", ReportFormatUtils.statusLabel(filters.getStatus())),
                    new Kpi("Nguồn dữ liệu", "Dữ liệu đơn hàng"));
            writeChart(document, createOrderStatusChart(report));

            PdfPTable table = createTable(new float[]{0.45f, 0.85f, 1.8f, 1.1f, 1.4f, 1.25f, 1.7f});
            writeHeaders(table, "STT", "Mã đơn hàng", "Khách hàng", "Ngày đặt", "Tổng tiền", "Trạng thái",
                    "Phương thức thanh toán");
            int index = 1;
            for (OrderReportRowResponse row : safeList(report.getRows())) {
                addCell(table, String.valueOf(index++), Element.ALIGN_CENTER);
                addCell(table, "#" + safeLong(row.getOrderId()), Element.ALIGN_CENTER);
                addCell(table, row.getCustomerName());
                addCell(table, ReportFormatUtils.formatDate(row.getCreatedAt()), Element.ALIGN_CENTER);
                addCell(table, ReportFormatUtils.formatCurrency(row.getTotalAmount()), Element.ALIGN_RIGHT);
                addCell(table, row.getStatusLabel(), Element.ALIGN_CENTER);
                addCell(table, row.getPaymentMethod());
            }
            addTotalRow(table, "Tổng cộng", "", "", "",
                    ReportFormatUtils.formatCurrency(report.getTotalAmount()),
                    ReportFormatUtils.formatNumber(report.getTotalOrders()) + " đơn hàng", "");
            document.add(table);
            writeFooter(document, "Tổng số đơn hàng phù hợp: " + ReportFormatUtils.formatNumber(report.getTotalOrders()),
                    filters);
        } catch (DocumentException e) {
            throw new IOException("Không thể xuất PDF báo cáo đơn hàng", e);
        } finally {
            document.close();
        }
    }

    public void exportProducts(ProductSalesReportResponse report, AdminReportFilterRequest filters,
                               OutputStream outputStream) throws IOException {
        Document document = openDocument(outputStream);
        try {
            writeDocumentHeader(document, ReportFormatUtils.reportTitle("products"), filters);
            writeKpis(document,
                    new Kpi("Số sản phẩm hiển thị", ReportFormatUtils.formatNumber(safeList(report.getRows()).size())),
                    new Kpi("Tổng số lượng đã bán", ReportFormatUtils.formatNumber(report.getTotalQuantitySold())),
                    new Kpi("Tổng doanh thu", ReportFormatUtils.formatCurrency(report.getTotalRevenue())),
                    new Kpi("Giới hạn báo cáo", report.getTopLimit() + " sản phẩm"));
            writeChart(document, createProductChart(report));

            PdfPTable table = createTable(new float[]{0.5f, 0.9f, 2.8f, 1.2f, 1.3f, 1.5f});
            writeHeaders(table, "STT", "Mã sản phẩm", "Tên sản phẩm", "Danh mục", "Số lượng đã bán", "Doanh thu");
            int index = 1;
            for (ProductSalesReportRowResponse row : safeList(report.getRows())) {
                addCell(table, String.valueOf(index++), Element.ALIGN_CENTER);
                addCell(table, "#" + safeLong(row.getProductId()), Element.ALIGN_CENTER);
                addCell(table, row.getProductName());
                addCell(table, row.getCategory(), Element.ALIGN_CENTER);
                addCell(table, ReportFormatUtils.formatNumber(safeLong(row.getQuantitySold())), Element.ALIGN_RIGHT);
                addCell(table, ReportFormatUtils.formatCurrency(row.getRevenue()), Element.ALIGN_RIGHT);
            }
            addTotalRow(table, "Tổng cộng", "", "", "",
                    ReportFormatUtils.formatNumber(report.getTotalQuantitySold()),
                    ReportFormatUtils.formatCurrency(report.getTotalRevenue()));
            document.add(table);
            writeFooter(document, "Tổng số lượng sản phẩm đã bán: "
                    + ReportFormatUtils.formatNumber(report.getTotalQuantitySold()), filters);
        } catch (DocumentException e) {
            throw new IOException("Không thể xuất PDF báo cáo sản phẩm bán chạy", e);
        } finally {
            document.close();
        }
    }

    public void exportInventory(InventoryReportResponse report, AdminReportFilterRequest filters,
                                OutputStream outputStream) throws IOException {
        Document document = openDocument(outputStream);
        try {
            writeDocumentHeader(document, ReportFormatUtils.reportTitle("inventory"), filters);
            writeKpis(document,
                    new Kpi("Tổng sản phẩm", ReportFormatUtils.formatNumber(report.getTotalProducts())),
                    new Kpi("Còn hàng", ReportFormatUtils.formatNumber(report.getInStockProducts())),
                    new Kpi("Sắp hết hàng", ReportFormatUtils.formatNumber(report.getLowStockProducts())),
                    new Kpi("Hết hàng", ReportFormatUtils.formatNumber(report.getOutOfStockProducts())));
            writeChart(document, createInventoryChart(report));

            PdfPTable table = createTable(new float[]{0.45f, 0.85f, 2.7f, 1.2f, 1.1f, 1.4f, 1.45f});
            writeHeaders(table, "STT", "Mã sản phẩm", "Tên sản phẩm", "Danh mục", "Số lượng tồn", "Giá bán",
                    "Trạng thái tồn kho");
            int index = 1;
            for (InventoryReportRowResponse row : safeList(report.getRows())) {
                addCell(table, String.valueOf(index++), Element.ALIGN_CENTER);
                addCell(table, "#" + safeLong(row.getProductId()), Element.ALIGN_CENTER);
                addCell(table, row.getProductName());
                addCell(table, row.getCategory(), Element.ALIGN_CENTER);
                addCell(table, ReportFormatUtils.formatNumber(safeLong(row.getQuantity())), Element.ALIGN_RIGHT);
                addCell(table, ReportFormatUtils.formatCurrency(row.getPrice()), Element.ALIGN_RIGHT);
                addCell(table, row.getStockStatus(), Element.ALIGN_CENTER);
            }
            addTotalRow(table, "Tổng cộng", "", "", "",
                    ReportFormatUtils.formatNumber(report.getTotalProducts()),
                    ReportFormatUtils.formatCurrency(report.getInventoryValue()), "");
            document.add(table);
            writeFooter(document, "Giá trị tồn kho ước tính: "
                    + ReportFormatUtils.formatCurrency(report.getInventoryValue()), filters);
        } catch (DocumentException e) {
            throw new IOException("Không thể xuất PDF báo cáo tồn kho", e);
        } finally {
            document.close();
        }
    }

    private Document openDocument(OutputStream outputStream) throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 24, 24, 28, 28);
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            return document;
        } catch (DocumentException e) {
            throw new IOException("Không thể khởi tạo tài liệu PDF", e);
        }
    }

    private void writeDocumentHeader(Document document, String title, AdminReportFilterRequest filters)
            throws DocumentException {
        Paragraph brand = new Paragraph("ELECTRO", font(14, com.lowagie.text.Font.BOLD, DARK));
        brand.setAlignment(Element.ALIGN_LEFT);
        document.add(brand);

        Paragraph titleParagraph = new Paragraph(uppercaseTitle(title), font(17, com.lowagie.text.Font.BOLD, PRIMARY));
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(8);
        document.add(titleParagraph);

        PdfPTable meta = createTable(new float[]{1.6f, 4.2f, 1.4f, 2.2f});
        addMetaCell(meta, "Khoảng thời gian lọc");
        addMetaCell(meta, buildPeriod(filters));
        addMetaCell(meta, "Từ ngày");
        addMetaCell(meta, buildFromDate(filters));
        addMetaCell(meta, "Đến ngày");
        addMetaCell(meta, buildToDate(filters));
        addMetaCell(meta, "Ngày xuất báo cáo");
        addMetaCell(meta, ReportFormatUtils.formatDateTime(LocalDateTime.now()));
        addMetaCell(meta, "Người lập báo cáo");
        addMetaCell(meta, ReportFormatUtils.exportedBy(filters.getExportedBy()));
        addMetaCell(meta, "Hệ thống");
        addMetaCell(meta, "Hệ thống Electro");
        addMetaCell(meta, "Ghi chú trạng thái đơn hàng");
        PdfPCell note = new PdfPCell(new Phrase(buildStatusNote(filters), font(9, com.lowagie.text.Font.NORMAL, TEXT)));
        note.setColspan(3);
        note.setPadding(5);
        note.setBorderColor(BORDER);
        meta.addCell(note);
        meta.setSpacingAfter(10);
        document.add(meta);
    }

    private void writeKpis(Document document, Kpi... kpis) throws DocumentException {
        PdfPTable table = createTable(new float[]{1f, 1f, 1f, 1f});
        table.setSpacingAfter(12);
        for (Kpi kpi : kpis) {
            PdfPCell cell = new PdfPCell();
            cell.setPadding(8);
            cell.setBorderColor(BORDER);
            cell.setBackgroundColor(KPI_BACKGROUND);
            Paragraph label = new Paragraph(kpi.label(), font(8, com.lowagie.text.Font.BOLD, TEXT));
            label.setAlignment(Element.ALIGN_CENTER);
            Paragraph value = new Paragraph(kpi.value(), font(11, com.lowagie.text.Font.BOLD, DARK));
            value.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(label);
            cell.addElement(value);
            table.addCell(cell);
        }
        document.add(table);
    }

    private void writeChart(Document document, JFreeChart chart) throws IOException, DocumentException {
        ByteArrayOutputStream chartOutput = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartOutput, chart, 900, 300);
        Image image = Image.getInstance(chartOutput.toByteArray());
        image.scaleToFit(760, 250);
        image.setAlignment(Image.ALIGN_CENTER);
        image.setSpacingAfter(12);
        document.add(image);
    }

    private JFreeChart createRevenueChart(RevenueReportResponse report) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (RevenueReportRowResponse row : safeList(report.getRows())) {
            dataset.addValue(safeDouble(row.getRevenue()), "Doanh thu", ReportFormatUtils.formatDate(row.getReportDate()));
        }
        return createLineChart("Doanh thu theo thời gian", "Thời gian", "Doanh thu (VNĐ)", dataset);
    }

    private JFreeChart createOrderStatusChart(OrderReportResponse report) {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (OrderReportRowResponse row : safeList(report.getRows())) {
            String status = row.getStatusLabel() == null ? "Không xác định" : row.getStatusLabel();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
        }
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return createPieChart("Tỷ lệ đơn hàng theo trạng thái", dataset);
    }

    private JFreeChart createProductChart(ProductSalesReportResponse report) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ProductSalesReportRowResponse row : safeList(report.getRows())) {
            dataset.addValue(safeLong(row.getQuantitySold()), "Số lượng đã bán", shorten(row.getProductName(), 34));
        }
        return createBarChart("Sản phẩm bán chạy theo số lượng", "Sản phẩm", "Số lượng đã bán", dataset,
                PlotOrientation.HORIZONTAL);
    }

    private JFreeChart createInventoryChart(InventoryReportResponse report) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(safeLong(report.getInStockProducts()), "Số sản phẩm", "Còn hàng");
        dataset.addValue(safeLong(report.getLowStockProducts()), "Số sản phẩm", "Sắp hết hàng");
        dataset.addValue(safeLong(report.getOutOfStockProducts()), "Số sản phẩm", "Hết hàng");
        return createBarChart("Số sản phẩm theo trạng thái tồn kho", "Trạng thái tồn kho", "Số sản phẩm", dataset,
                PlotOrientation.VERTICAL);
    }

    private JFreeChart createBarChart(String title, String categoryLabel, String valueLabel,
                                      DefaultCategoryDataset dataset, PlotOrientation orientation) {
        JFreeChart chart = ChartFactory.createBarChart(title, categoryLabel, valueLabel, dataset, orientation,
                false, true, false);
        styleChartBase(chart);
        chart.getTitle().setFont(chartBoldFont);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(BORDER);
        plot.setRangeGridlinePaint(BORDER);
        plot.getDomainAxis().setLabelFont(chartFont);
        plot.getDomainAxis().setTickLabelFont(chartFont.deriveFont(9f));
        plot.getRangeAxis().setLabelFont(chartFont);
        plot.getRangeAxis().setTickLabelFont(chartFont.deriveFont(9f));
        if (plot.getRenderer() instanceof BarRenderer renderer) {
            renderer.setSeriesPaint(0, SECONDARY);
            renderer.setDrawBarOutline(false);
        } else {
            plot.getRenderer().setSeriesPaint(0, SECONDARY);
        }
        return chart;
    }

    private JFreeChart createLineChart(String title, String categoryLabel, String valueLabel,
                                       DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createLineChart(title, categoryLabel, valueLabel, dataset,
                PlotOrientation.VERTICAL, false, true, false);
        styleChartBase(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(BORDER);
        plot.setRangeGridlinePaint(BORDER);
        plot.getDomainAxis().setLabelFont(chartFont);
        plot.getDomainAxis().setTickLabelFont(chartFont.deriveFont(9f));
        plot.getRangeAxis().setLabelFont(chartFont);
        plot.getRangeAxis().setTickLabelFont(chartFont.deriveFont(9f));
        if (plot.getRenderer() instanceof LineAndShapeRenderer renderer) {
            renderer.setSeriesPaint(0, PRIMARY);
            renderer.setSeriesShapesVisible(0, true);
        } else {
            plot.getRenderer().setSeriesPaint(0, PRIMARY);
        }
        return chart;
    }

    private JFreeChart createPieChart(String title, DefaultPieDataset<String> dataset) {
        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        styleChartBase(chart);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(BORDER);
        plot.setLabelFont(chartFont.deriveFont(9f));
        plot.setLabelBackgroundPaint(LIGHT_GREY);
        plot.setLabelOutlinePaint(BORDER);
        plot.setLabelShadowPaint(null);
        int index = 0;
        for (Comparable<?> key : dataset.getKeys()) {
            plot.setSectionPaint(key, CHART_COLORS[index % CHART_COLORS.length]);
            index++;
        }
        return chart;
    }

    private void styleChartBase(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(chartBoldFont);
        chart.getTitle().setPaint(DARK);
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(chartFont.deriveFont(9f));
            chart.getLegend().setBackgroundPaint(Color.WHITE);
        }
    }

    private PdfPTable createTable(float[] widths) throws DocumentException {
        PdfPTable table = new PdfPTable(widths.length);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        return table;
    }

    private void writeHeaders(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font(8, com.lowagie.text.Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(DARK);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            cell.setBorderColor(DARK);
            table.addCell(cell);
        }
        table.setHeaderRows(1);
    }

    private void addCell(PdfPTable table, String value) {
        addCell(table, value, Element.ALIGN_LEFT);
    }

    private void addCell(PdfPTable table, String value, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value == null || value.isBlank() ? "-" : value,
                font(8, com.lowagie.text.Font.NORMAL, TEXT)));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(BORDER);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String... values) {
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value,
                    font(8, com.lowagie.text.Font.BOLD, TEXT)));
            cell.setBackgroundColor(TOTAL_BACKGROUND);
            cell.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5);
            cell.setBorderColor(BORDER);
            table.addCell(cell);
        }
    }

    private void addMetaCell(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value == null || value.isBlank() ? "-" : value,
                font(9, com.lowagie.text.Font.NORMAL, TEXT)));
        cell.setBackgroundColor(LIGHT_GREY);
        cell.setPadding(5);
        cell.setBorderColor(BORDER);
        table.addCell(cell);
    }

    private void writeFooter(Document document, String summary, AdminReportFilterRequest filters)
            throws DocumentException {
        Paragraph title = new Paragraph("Tổng kết", font(10, com.lowagie.text.Font.BOLD, PRIMARY));
        title.setSpacingBefore(12);
        title.setSpacingAfter(4);
        document.add(title);
        document.add(new Paragraph(summary, font(9, com.lowagie.text.Font.NORMAL, TEXT)));
        document.add(new Paragraph(AUTO_NOTE, font(9, com.lowagie.text.Font.ITALIC, MUTED)));

        PdfPTable signatures = createTable(new float[]{1f, 1f});
        signatures.setSpacingBefore(16);
        addSignatureCell(signatures, "Người lập báo cáo", ReportFormatUtils.exportedBy(filters.getExportedBy()));
        addSignatureCell(signatures, "Quản trị viên", ReportFormatUtils.exportedBy(filters.getExportedBy()));
        document.add(signatures);
    }

    private void addSignatureCell(PdfPTable table, String title, String name) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(Color.WHITE);
        cell.setPadding(6);
        Paragraph titleParagraph = new Paragraph(title, font(9, com.lowagie.text.Font.BOLD, TEXT));
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        Paragraph spacer = new Paragraph("\n\n", font(9, com.lowagie.text.Font.NORMAL, TEXT));
        Paragraph nameParagraph = new Paragraph(name, font(9, com.lowagie.text.Font.NORMAL, TEXT));
        nameParagraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(titleParagraph);
        cell.addElement(spacer);
        cell.addElement(nameParagraph);
        table.addCell(cell);
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

    private com.lowagie.text.Font font(float size, int style, Color color) {
        BaseFont selectedFont = switch (style) {
            case com.lowagie.text.Font.BOLD, com.lowagie.text.Font.BOLDITALIC -> boldBaseFont;
            case com.lowagie.text.Font.ITALIC -> regularBaseFont;
            default -> regularBaseFont;
        };
        int pdfStyle = style == com.lowagie.text.Font.ITALIC ? com.lowagie.text.Font.ITALIC : com.lowagie.text.Font.NORMAL;
        return new com.lowagie.text.Font(selectedFont, size, pdfStyle, color);
    }

    private BaseFont loadBaseFont(String fontPath, String resourceName) throws IOException {
        byte[] bytes = readFontBytes(fontPath, resourceName);
        try {
            return BaseFont.createFont(resourceName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, bytes, null);
        } catch (Exception e) {
            throw new IOException("Không thể nhúng font Unicode tiếng Việt vào PDF: " + resourceName, e);
        }
    }

    private java.awt.Font loadChartFont(String fontPath, String resourceName, int style, float size) throws IOException {
        byte[] bytes = readFontBytes(fontPath, resourceName);
        try (InputStream inputStream = new java.io.ByteArrayInputStream(bytes)) {
            return java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, inputStream).deriveFont(style, size);
        } catch (Exception e) {
            throw new IOException("Không thể load font Unicode cho biểu đồ PDF: " + resourceName, e);
        }
    }

    private byte[] readFontBytes(String fontPath, String resourceName) throws IOException {
        if (fontPath != null && !fontPath.isBlank()) {
            Path path = Path.of(fontPath);
            if (Files.isRegularFile(path)) {
                return Files.readAllBytes(path);
            }
        }

        String resourcePath = "/fonts/" + resourceName;
        try (InputStream inputStream = ReportPdfExporter.class.getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                return inputStream.readAllBytes();
            }
        }
        throw new IOException("Không tìm thấy font Unicode tiếng Việt: " + resourcePath);
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

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0D : value;
    }

    private static Color hex(String value) {
        String hex = value.startsWith("#") ? value.substring(1) : value;
        return new Color(
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16));
    }

    private record Kpi(String label, String value) {
    }
}
