package com.daizuongkk.web.service;

import com.daizuongkk.web.dto.request.AdminProductSearchRequest;
import com.daizuongkk.web.dto.request.SearchProductRequest;
import com.daizuongkk.web.dto.response.ProductResponse;
import com.daizuongkk.web.model.Category;
import com.daizuongkk.web.model.Product;
import com.daizuongkk.web.model.ProductImg;
import com.daizuongkk.web.repository.ProductImgRepository;
import com.daizuongkk.web.repository.ProductRepository;
import com.daizuongkk.web.repository.ReviewRepository;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

public class ProductService {

    private final ModelMapper modelMapper = new ModelMapper();

    private final ProductRepository productRepository;
    private final ProductImgRepository productImgRepository;
    private final ReviewRepository reviewRepository;

    public ProductService() {
        this.productRepository = new ProductRepository();
        this.productImgRepository = new ProductImgRepository();
        this.reviewRepository = new ReviewRepository();
    }

    public ProductService(ProductRepository productRepository,
                          ProductImgRepository productImgRepository,
                          ReviewRepository reviewRepository) {
        this.productRepository = productRepository != null ? productRepository : new ProductRepository();
        this.productImgRepository = productImgRepository != null ? productImgRepository : new ProductImgRepository();
        this.reviewRepository = reviewRepository != null ? reviewRepository : new ReviewRepository();
    }


    public List<ProductResponse> getAllProducts() {

        List<Product> products = productRepository.findAll();
        List<ProductResponse> productResponseList = new ArrayList<>();

        for (Product product : products) {
            productResponseList.add(productToProductResponse(product));
        }
        return productResponseList;
    }

    public List<ProductResponse> getProductsByPage(int page, int size) {
        List<Product> products =  productRepository.findPage(page, size);
        List<ProductResponse> productResponseList = new ArrayList<>();

        for (Product product : products) {
            productResponseList.add(productToProductResponse(product));
        }
        return productResponseList;
    }

    public List<ProductResponse> getAdminProducts(AdminProductSearchRequest filters, int page, int size) {
        return productRepository.findAdminPage(filters, page, size)
                .stream()
                .map(this::productToProductResponse)
                .toList();
    }

    public Long countAdminProducts(AdminProductSearchRequest filters) {
        return productRepository.countAdmin(filters);
    }

    public List<ProductResponse> getLowStockProducts(int limit) {
        return productRepository.findLowStock(limit)
                .stream()
                .map(this::productToProductResponse)
                .toList();
    }

    public ProductResponse createProduct(Product product, List<String> imageUrls) {
        Product created = productRepository.save(product);
        if (created == null) {
            return null;
        }
        productImgRepository.createBatch(created.getId(), imageUrls);
        return productToProductResponse(created);
    }

    public boolean updateProduct(Product product, List<String> imageUrls) {
        boolean updated = productRepository.update(product);
        if (updated) {
            productImgRepository.createBatch(product.getId(), imageUrls);
        }
        return updated;
    }

    public Product getProductModelById(Long id) {
        return productRepository.findById(id);
    }

    public Product getAdminProductModelById(Long id) {
        return productRepository.findByIdIncludingDeleted(id);
    }

    public List<String> getProductImageUrls(Long productId) {
        return productImgRepository.findUrlsByProductId(productId);
    }

    public List<ProductImg> getProductImages(Long productId) {
        return productImgRepository.findByProductId(productId);
    }

    public int addProductImages(Long productId, List<String> imageUrls) {
        return productImgRepository.createBatch(productId, imageUrls);
    }

    public int deleteProductImages(Long productId, List<Long> imageIds) {
        return productImgRepository.deleteByIds(productId, imageIds);
    }

    public boolean deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }

    public ProductResponse getProductById(Long id) {

        return productToProductResponse(productRepository.findById(id))  ;
    }

    public List<ProductResponse> getProductsByCategory(String category) {


        List<Product> products = productRepository.findByCategory(category);
        List<ProductResponse> productResponses = new ArrayList<>();
        for (Product product : products) {
            productResponses.add(productToProductResponse(product));
        }
        return productResponses;
    }

    public List<Product> searchProductsByName(String keyword) {
        return productRepository.searchByName(keyword);
    }

    public List<ProductResponse> getLatestProducts(int limit) {

        List<ProductResponse> productResponses = new ArrayList<>();

        List<Product> products =  productRepository.findLatest(limit);

        for (Product product : products) {
            productResponses.add(productToProductResponse(product));
        }
        return productResponses;
    }

    public Long countProducts() {
        return productRepository.countAll();
    }


    private ProductResponse productToProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse productResponse = this.modelMapper.map(product, ProductResponse.class);
        List<String> imageUrls = productImgRepository.findUrlsByProductId(product.getId());
        productResponse.setCategory(Category.getNameByCode(product.getCategory()));
        productResponse.setDeleted(product.getDeleted());

        productResponse.setImageUrl(imageUrls);

        Double reviewScore = reviewRepository.findAverageScoreByProductId(product.getId());
        productResponse.setReviewScore(reviewScore);


        return productResponse;

    }

    public Long countProductsByFilter(SearchProductRequest filters) {

        return productRepository.countByFilter(filters);
    }

    public List<ProductResponse> getProductsByFilter(int currentPage, int size, SearchProductRequest filters) {
        List<Product> products = productRepository.findByFilter(currentPage, size, filters);
        return products.stream().map(this::productToProductResponse).toList();

    }
}
