package com.example.ecommerce.services.brand;

import com.example.ecommerce.dtos.BrandDTO;
import com.example.ecommerce.dtos.CategoryDTO;
import com.example.ecommerce.models.Brand;

import java.util.List;

public interface BrandService {
    Brand createBrand(BrandDTO brandDTO);
    Brand getBrandById(long id);
    List<Brand> getAllBrands();
    List<Brand> getBrandByCategory(String name);
    Brand updateBrand(long id, BrandDTO brandDTO);
    void deleteBrand(long id);
}
