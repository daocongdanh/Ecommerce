package com.example.ecommerce.services.product;

import com.example.ecommerce.dtos.ProductAttributeDTO;
import com.example.ecommerce.dtos.ProductDTO;
import com.example.ecommerce.exceptions.ResourceNotFoundException;
import com.example.ecommerce.models.*;
import com.example.ecommerce.repositories.*;
import com.example.ecommerce.responses.PageResponse;
import com.example.ecommerce.responses.ProductResponse;
import com.example.ecommerce.services.brand.BrandService;
import com.example.ecommerce.services.category.CategoryService;
import com.example.ecommerce.services.product.ProductService;
import com.example.ecommerce.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final AttributeRepository attributeRepository;
    private final BrandService brandService;
    private final SearchRepository searchRepository;
    private final CategoryService categoryService;
    private final CommentRepository commentRepository;
    @Override
    public PageResponse searchProduct(
            int page, int limit, String brand,String category, String[] search, boolean active,
            String... sort) {
        return searchRepository.searchProduct(page, limit, brand,category, search, active, sort);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(p -> ProductResponse.fromProduct(
                        p,calcAvgRate(commentRepository.findAllByProduct(p)), null))
                .toList();
    }

    @Override
    public Product findById(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id = " + id));
    }

    @Override
    @Transactional
    public ProductResponse updateProductStatus(long id, boolean active) {
        Product product = findById(id);
        product.setActive(active);
        productRepository.save(product);
        return ProductResponse.fromProduct(product, 0,
                productAttributeRepository.findAllByProduct(product));
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product p = findById(id);
        return ProductResponse.fromProduct(p,
                calcAvgRate(commentRepository.findAllByProduct(p)),
                productAttributeRepository.findAllByProduct(p));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductDTO productDTO) {
        Brand brand = brandService.getBrandById(productDTO.getBrandId());
        Category category = categoryService.getCategoryById(productDTO.getCategoryId());
        Product product = productRepository.save(Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .discount(productDTO.getDiscount())
                .stock(productDTO.getStock())
                .viewCount(0L)
                .description(productDTO.getDescription())
                .discountForMember(productDTO.getDiscountForMember())
                .active(productDTO.isActive())
                .brand(brand)
                .category(category)
                .build());
        List<ProductAttribute> productAttributes = new ArrayList<>();
        if(productDTO.getAttributes() != null){
            for(ProductAttributeDTO pad : productDTO.getAttributes()){
                Attribute attribute = attributeRepository.findByName(pad.getAttribute());
                ProductAttribute productAttribute = ProductAttribute.builder()
                        .attribute(attribute)
                        .product(product)
                        .value(pad.getValue())
                        .slug(StringUtil.normalizeString(pad.getValue()))
                        .build();
                productAttributes.add(productAttributeRepository.save(productAttribute));
            }
        }
        return ProductResponse.fromProduct(product,
                calcAvgRate(commentRepository.findAllByProduct(product)),
                productAttributes);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(long id, ProductDTO productDTO) {
        Product product = findById(id);
        Brand brand = brandService.getBrandById(productDTO.getBrandId());
        Category category = categoryService.getCategoryById(productDTO.getCategoryId());
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setDiscount(productDTO.getDiscount());
        product.setStock(productDTO.getStock());
        product.setDescription(productDTO.getDescription());
        product.setDiscountForMember(productDTO.getDiscountForMember());
        product.setActive(productDTO.isActive());
        product.setBrand(brand);
        product.setCategory(category);
        productRepository.save(product);
        List<ProductAttribute> productAttributes = new ArrayList<>();
        if(productDTO.getAttributes() != null){
            for(ProductAttributeDTO pad : productDTO.getAttributes()){
                Attribute attribute = attributeRepository.findByName(pad.getAttribute());
                ProductAttribute productAttribute =
                        productAttributeRepository.findByProductAndAttribute(product, attribute);
                productAttribute.setValue(pad.getValue());
                productAttribute.setSlug(StringUtil.normalizeString(pad.getValue()));
                productAttributes.add(productAttributeRepository.save(productAttribute));
            }
        }
        return ProductResponse.fromProduct(product,
                calcAvgRate(commentRepository.findAllByProduct(product)), productAttributes);
    }

    @Override
    @Transactional
    public void updateViewCount(long id) {
        Product product = findById(id);
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
    }

    private Double calcAvgRate(List<Comment> comments){
        return comments.stream()
                .collect(Collectors.averagingDouble(Comment::getRate));
    }
}
