package com.example.cruddemo.controller;

import com.example.cruddemo.dto.ProductCreateRequest;
import com.example.cruddemo.dto.ProductResponse;
import com.example.cruddemo.dto.ProductUpdateRequest;
import com.example.cruddemo.exception.ProductNotFoundException;
import com.example.cruddemo.service.ProductService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productResponse = new ProductResponse(
                1L,
                "PROD-100",
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void createProduct_WithValidData_ShouldReturn201AndLocation() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(
                "PROD-100",
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10
        );

        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/products/1")))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.sku").value("PROD-100"))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService, times(1)).createProduct(any(ProductCreateRequest.class));
    }

    @Test
    void createProduct_WithInvalidData_ShouldReturn400AndProblemDetail() throws Exception {
        // Invalid data: SKU is empty, price is negative, name is too short
        ProductCreateRequest request = new ProductCreateRequest(
                "",
                "A",
                "Test Description",
                new BigDecimal("-5.00"),
                -1
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Constraint Violation"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasKey("sku")))
                .andExpect(jsonPath("$.errors", hasKey("name")))
                .andExpect(jsonPath("$.errors", hasKey("price")))
                .andExpect(jsonPath("$.errors", hasKey("stockQuantity")));

        verify(productService, never()).createProduct(any());
    }

    @Test
    void getProductById_WhenExists_ShouldReturn200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.sku").value("PROD-100"));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void getProductById_WhenDoesNotExist_ShouldReturn404AndProblemDetail() throws Exception {
        when(productService.getProductById(999L)).thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Product not found with id: 999"));

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    void updateProduct_WithValidData_ShouldReturn200() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Updated Name",
                "Updated Description",
                new BigDecimal("149.99"),
                20
        );

        ProductResponse updatedResponse = new ProductResponse(
                1L,
                "PROD-100",
                "Updated Name",
                "Updated Description",
                new BigDecimal("149.99"),
                20,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(productService.updateProduct(eq(1L), any(ProductUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.price").value(149.99));

        verify(productService, times(1)).updateProduct(eq(1L), any(ProductUpdateRequest.class));
    }

    @Test
    void deleteProduct_WhenExists_ShouldReturn24NoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }
}
