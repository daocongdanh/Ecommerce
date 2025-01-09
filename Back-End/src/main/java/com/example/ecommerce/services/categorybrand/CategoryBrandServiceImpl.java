package com.example.ecommerce.services.categorybrand;

import com.example.ecommerce.repositories.CategoryBrandRepository;
import com.example.ecommerce.responses.CategoryBrandResponse;
import com.example.ecommerce.services.categorybrand.CategoryBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryBrandServiceImpl implements CategoryBrandService {
    private final CategoryBrandRepository categoryBrandRepository;
    @Override
    public List<CategoryBrandResponse> getAllCategoryBrands() {
        return categoryBrandRepository.findAll()
                .stream()
                .map(CategoryBrandResponse::fromCategoryBrand)
                .toList();
    }
}
