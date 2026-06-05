package com.example.cruddemo.service;

import com.example.cruddemo.dto.ProductCreateRequest;
import com.example.cruddemo.dto.ProductResponse;
import com.example.cruddemo.dto.ProductUpdateRequest;
import com.example.cruddemo.exception.ProductAlreadyExistsException;
import com.example.cruddemo.exception.ProductNotFoundException;
import com.example.cruddemo.model.Product;
import com.example.cruddemo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductCreateRequest createRequest;
    private ProductUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .sku("PROD-100")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = new ProductCreateRequest(
                "PROD-100",
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10
        );

        updateRequest = new ProductUpdateRequest(
                "Updated Name",
                "Updated Description",
                new BigDecimal("149.99"),
                20
        );
    }

    @Test
    void createProduct_ShouldSaveAndReturnResponse_WhenSkuIsUnique() {
        when(productRepository.existsBySku(any())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(createRequest);

        assertNotNull(response);
        assertEquals(product.getId(), response.id());
        assertEquals(product.getSku(), response.sku());
        verify(productRepository, times(1)).existsBySku(createRequest.sku());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldThrowException_WhenSkuExists() {
        when(productRepository.existsBySku(any())).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productService.createProduct(createRequest));
        verify(productRepository, times(1)).existsBySku(createRequest.sku());
        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenIdExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_ShouldThrowException_WhenIdDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(999L));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnResponse_WhenIdExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Name", product.getName());
        assertEquals(new BigDecimal("149.99"), product.getPrice());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void updateProduct_ShouldThrowException_WhenIdDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(999L, updateRequest));
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_ShouldDelete_WhenIdExists() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenIdDoesNotExist() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(999L));

        verify(productRepository, times(1)).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllProducts_ShouldReturnPageOfResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<ProductResponse> result = productService.getAllProducts(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(product.getName(), result.getContent().get(0).name());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllProducts_WithSearchQuery_ShouldFilterByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findByNameContainingIgnoreCase("test", pageable)).thenReturn(page);

        Page<ProductResponse> result = productService.getAllProducts("test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("test", pageable);
    }
}
