package com.daizuongkk.web.service;

import com.daizuongkk.web.model.Product;
import com.daizuongkk.web.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, null, null);
    }

    @Test
    void searchProductsByNameShouldDelegateToRepository() {
        List<Product> expected = List.of(
                Product.builder().id(1L).name("iPhone 15").build(),
                Product.builder().id(2L).name("iPhone 15 Pro").build()
        );
        when(productRepository.searchByName("iphone")).thenReturn(expected);

        List<Product> actual = productService.searchProductsByName("iphone");

        assertEquals(expected, actual);
        verify(productRepository).searchByName("iphone");
    }
}


