package com.example.service;

import com.example.model.Category;
import com.example.payload.dto.SalonDTO;

import java.util.Set;

public interface CategoryService {
    Category saveCategory(Category category, SalonDTO salon);
    Set<Category> getAllCategoriesBySalon(Long id);
    Category getCategoryById(Long id) throws Exception;
    void deleteCategoryById(Long id,Long salonId) throws Exception;
    Category findByIdAndSalonId(Long id,Long salonId) throws Exception;
}
