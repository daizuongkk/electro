package com.daizuongkk.web.controller.admin;

import com.daizuongkk.web.dto.request.AdminProductSearchRequest;
import com.daizuongkk.web.dto.response.ProductResponse;
import com.daizuongkk.web.model.Brand;
import com.daizuongkk.web.model.Category;
import com.daizuongkk.web.model.Product;
import com.daizuongkk.web.service.CloudinaryService;
import com.daizuongkk.web.service.ProductService;
import com.daizuongkk.web.util.PaginationUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "AdminProductController", value = {"/admin/products", "/admin/products/form"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 60 * 1024 * 1024
)
public class AdminProductController extends BaseAdminServlet {

    private final ProductService productService = new ProductService();
    private final CloudinaryService cloudinaryService = new CloudinaryService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        if (request.getServletPath().endsWith("/form")) {
            loadProductForm(request);
            forward(request, response, "create-product.jsp");
            return;
        }

        loadInventory(request);
        forward(request, response, "inventory.jsp");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("delete-product".equals(action)) {
            productService.deleteProduct(parseLong(request.getParameter("id")));
            response.sendRedirect(request.getContextPath() + "/admin/products");
            return;
        }

        saveProduct(request, response);
    }

    private void loadInventory(HttpServletRequest request) {
        int page = PaginationUtils.parsePositiveInt(request.getParameter("p"), 1);
        int size = PaginationUtils.parsePositiveInt(request.getParameter("size"), 10);
        AdminProductSearchRequest filters = buildFilters(request);

        long totalProducts = productService.countAdminProducts(filters);
        int totalPages = Math.max(1, (int) Math.ceil(totalProducts / (double) size));
        if (page > totalPages) {
            page = totalPages;
        }

        request.setAttribute("products", productService.getAdminProducts(filters, page, size));
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalProducts", totalProducts);
        setFilterAttributes(request, filters);
        request.setAttribute("categories", Category.getAlls());
        request.setAttribute("brands", Brand.getAlls());
    }

    private void loadProductForm(HttpServletRequest request) {
        Long id = parseLong(request.getParameter("id"));
        if (id != null) {
            Product product = productService.getProductModelById(id);
            request.setAttribute("productForm", product);
            request.setAttribute("productImages", productService.getProductImages(id));
            request.setAttribute("editMode", product != null);
        } else {
            request.setAttribute("editMode", false);
        }
        request.setAttribute("categories", Category.getAlls());
        request.setAttribute("brands", Brand.getAlls());
    }

    private void saveProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long id = parseLong(request.getParameter("id"));
        Product product = Product.builder()
                .id(id)
                .name(trim(request.getParameter("name")))
                .description(trim(request.getParameter("description")))
                .detail(trim(request.getParameter("detail")))
                .summary(trim(request.getParameter("summary")))
                .price(parseDouble(request.getParameter("price")))
                .brand(trim(request.getParameter("brand")))
                .category(trim(request.getParameter("category")))
                .promotion(parseLongOrDefault(request.getParameter("promotion"), 0L))
                .quantity(parseLongOrDefault(request.getParameter("quantity"), 0L))
                .build();

        List<String> uploadedImageUrls;
        try {
            uploadedImageUrls = uploadImageFiles(request);
        } catch (Exception e) {
            request.setAttribute("error", "Không thể upload ảnh lên Cloudinary: " + e.getMessage());
            request.setAttribute("productForm", product);
            request.setAttribute("productImages", id == null ? List.of() : productService.getProductImages(id));
            request.setAttribute("editMode", id != null);
            request.setAttribute("categories", Category.getAlls());
            request.setAttribute("brands", Brand.getAlls());
            forward(request, response, "create-product.jsp");
            return;
        }

        boolean ok;
        if (id == null) {
            ProductResponse created = productService.createProduct(product, uploadedImageUrls);
            ok = created != null;
        } else {
            ok = productService.updateProduct(product, List.of());
            if (ok) {
                productService.deleteProductImages(id, parseDeletedImageIds(request.getParameterValues("deletedImageId")));
                productService.addProductImages(id, uploadedImageUrls);
            }
        }

        if (!ok) {
            request.setAttribute("error", "Không thể lưu sản phẩm. Vui lòng kiểm tra dữ liệu nhập.");
            request.setAttribute("productForm", product);
            request.setAttribute("productImages", id == null ? List.of() : productService.getProductImages(id));
            request.setAttribute("editMode", id != null);
            request.setAttribute("categories", Category.getAlls());
            request.setAttribute("brands", Brand.getAlls());
            forward(request, response, "create-product.jsp");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/products");
    }

    private AdminProductSearchRequest buildFilters(HttpServletRequest request) {
        return AdminProductSearchRequest.builder()
                .keyword(trim(request.getParameter("keyword")))
                .category(trim(request.getParameter("category")))
                .brand(trim(request.getParameter("brand")))
                .minPrice(parseDouble(request.getParameter("minPrice")))
                .maxPrice(parseDouble(request.getParameter("maxPrice")))
                .minQuantity(parseLong(request.getParameter("minQuantity")))
                .maxQuantity(parseLong(request.getParameter("maxQuantity")))
                .build();
    }

    private void setFilterAttributes(HttpServletRequest request, AdminProductSearchRequest filters) {
        request.setAttribute("keyword", filters.getKeyword() == null ? "" : filters.getKeyword());
        request.setAttribute("selectedCategory", filters.getCategory() == null ? "" : filters.getCategory());
        request.setAttribute("selectedBrand", filters.getBrand() == null ? "" : filters.getBrand());
        request.setAttribute("minPrice", filters.getMinPrice());
        request.setAttribute("maxPrice", filters.getMaxPrice());
        request.setAttribute("minQuantity", filters.getMinQuantity());
        request.setAttribute("maxQuantity", filters.getMaxQuantity());
    }

    private List<String> uploadImageFiles(HttpServletRequest request) throws IOException, ServletException, InterruptedException {
        List<String> imageUrls = new ArrayList<>();
        for (Part part : request.getParts()) {
            if (!"imageFiles".equals(part.getName()) || part.getSize() <= 0) {
                continue;
            }
            String contentType = part.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                throw new IOException("File không phải ảnh: " + part.getSubmittedFileName());
            }
            String uploadedUrl = cloudinaryService.uploadImage(part);
            if (uploadedUrl != null && !uploadedUrl.isBlank()) {
                imageUrls.add(uploadedUrl);
            }
        }
        return imageUrls;
    }

    private List<Long> parseDeletedImageIds(String[] values) {
        if (values == null || values.length == 0) {
            return List.of();
        }

        List<Long> ids = new ArrayList<>();
        for (String value : values) {
            Long id = parseLong(value);
            if (id != null && id > 0) {
                ids.add(id);
            }
        }
        return ids;
    }
}
