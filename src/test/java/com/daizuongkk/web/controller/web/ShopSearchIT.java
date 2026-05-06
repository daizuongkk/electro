package com.daizuongkk.web.controller.web;

import com.daizuongkk.web.dto.request.SearchProductRequest;
import com.daizuongkk.web.dto.response.ProductResponse;
import com.daizuongkk.web.service.ProductService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ShopSearchIT {

    private ShopController controller;
    private FakeProductService fakeProductService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ShopController();
        fakeProductService = new FakeProductService();
        setField(controller, "productService", fakeProductService);
    }

    @Test
    void doGetShouldBuildSearchFilterAndForwardStorePage() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("name")).thenReturn("iphone");
        when(request.getParameter("sortBy")).thenReturn("price_asc");
        when(request.getParameterValues("category")).thenReturn(new String[]{"PHONE"});
        when(request.getParameterValues("brand")).thenReturn(new String[]{"APPLE"});
        when(request.getParameter("minPrice")).thenReturn("1000000");
        when(request.getParameter("maxPrice")).thenReturn("30000000");
        when(request.getParameter("size")).thenReturn("6");
        when(request.getParameter("page")).thenReturn("2");
        when(request.getRequestDispatcher("views/pages/store.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        assertNotNull(fakeProductService.lastFilter);
        assertEquals("iphone", fakeProductService.lastFilter.getName());
        assertEquals(List.of("PHONE"), fakeProductService.lastFilter.getCategories());
        assertEquals(List.of("APPLE"), fakeProductService.lastFilter.getBrands());
        assertEquals(2, fakeProductService.lastPage);
        assertEquals(6, fakeProductService.lastSize);

        verify(request).setAttribute(eq("products"), any());
        verify(request).setAttribute("filterName", "iphone");
        verify(dispatcher).forward(request, response);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class FakeProductService extends ProductService {
        private SearchProductRequest lastFilter;
        private int lastPage;
        private int lastSize;

        FakeProductService() {
            super(null, null, null);
        }

        @Override
        public Long countProductsByFilter(SearchProductRequest filters) {
            this.lastFilter = filters;
            return 12L;
        }

        @Override
        public List<ProductResponse> getProductsByFilter(int currentPage, int size, SearchProductRequest filters) {
            this.lastPage = currentPage;
            this.lastSize = size;
            this.lastFilter = filters;
            return List.of(ProductResponse.builder().id(1L).name("iPhone 15").build());
        }
    }
}

