package com.example.cruddemo.integration;

import com.example.cruddemo.dto.ProductCreateRequest;
import com.example.cruddemo.dto.ProductUpdateRequest;
import com.example.cruddemo.model.Product;
import com.example.cruddemo.repository.ProductRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        productRepository.deleteAll();
    }

    @Test
    void shouldPerformFullCrudLifecycle() throws Exception {
        // 1. Create a product
        ProductCreateRequest createRequest = new ProductCreateRequest(
                "PROD-999",
                "Integration Product",
                "Integration Desc",
                new BigDecimal("199.99"),
                50
        );

        String responseContent = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.sku").value("PROD-999"))
                .andExpect(jsonPath("$.name").value("Integration Product"))
                .andReturn().getResponse().getContentAsString();

        // Parse ID from response
        Long id = objectMapper.readTree(responseContent).get("id").asLong();

        // 2. Read the product by ID
        mockMvc.perform(get("/api/v1/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("PROD-999"))
                .andExpect(jsonPath("$.name").value("Integration Product"));

        // 3. Update the product
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                "Updated Name",
                "Updated Desc",
                new BigDecimal("299.99"),
                100
        );

        mockMvc.perform(put("/api/v1/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.price").value(299.99))
                .andExpect(jsonPath("$.stockQuantity").value(100));

        // 4. List products (search for Updated Name)
        mockMvc.perform(get("/api/v1/products?search=updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Updated Name"));

        // 5. Delete the product
        mockMvc.perform(delete("/api/v1/products/" + id))
                .andExpect(status().isNoContent());

        // 6. Verify product no longer exists
        mockMvc.perform(get("/api/v1/products/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflict_WhenCreatingDuplicateSku() throws Exception {
        Product existingProduct = Product.builder()
                .sku("PROD-777")
                .name("Existing")
                .price(BigDecimal.ONE)
                .stockQuantity(1)
                .build();
        productRepository.save(existingProduct);

        ProductCreateRequest createRequest = new ProductCreateRequest(
                "PROD-777",
                "Duplicate SKU Product",
                "Desc",
                BigDecimal.TEN,
                5
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Product Already Exists"))
                .andExpect(jsonPath("$.detail").value("Product with SKU 'PROD-777' already exists"));
    }
}
