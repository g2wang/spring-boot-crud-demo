package com.example.cruddemo.service;

import com.example.cruddemo.dto.ProductCreateRequest;
import com.example.cruddemo.dto.ProductResponse;
import com.example.cruddemo.dto.ProductUpdateRequest;
import com.example.cruddemo.exception.ProductAlreadyExistsException;
import com.example.cruddemo.exception.ProductNotFoundException;
import com.example.cruddemo.model.Product;
import com.example.cruddemo.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new ProductAlreadyExistsException(request.sku());
        }

        Product product = mapToEntity(request);
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    public Page<ProductResponse> getAllProducts(String search, Pageable pageable) {
        Page<Product> productsPage;
        if (search != null && !search.trim().isEmpty()) {
            productsPage = productRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }
        return productsPage.map(this::mapToResponse);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private Product mapToEntity(ProductCreateRequest request) {
        return Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .build();
    }
}
