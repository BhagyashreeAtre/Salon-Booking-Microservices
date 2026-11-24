package com.example.service.impl;

import com.example.model.Category;
import com.example.payload.dto.SalonDTO;
import com.example.repository.CategoryRepository;
import com.example.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public Category saveCategory(Category category, SalonDTO salon) {
        Category newcategory=new Category();
        newcategory.setName(category.getName());
        newcategory.setSalonId(category.getSalonId());
        newcategory.setImage(category.getImage());
        return categoryRepository.save(newcategory);
    }

    @Override
    public Set<Category> getAllCategoriesBySalon(Long id) {
        return categoryRepository.findBySalonId(id);
    }

    @Override
    public Category getCategoryById(Long id) throws Exception {
        Category category=categoryRepository.findById(id).orElse(null);
        if(category==null){
            throw new Exception("Category does not exist with id "+id);
        }

        return category;
    }

    @Override
    public void deleteCategoryById(Long id,Long salonId) throws Exception {
       Category category=getCategoryById(id);
       if(!category.getSalonId().equals(salonId)){
           throw new Exception("You dont have permission to delete this category. ");
       }
       categoryRepository.deleteById(id);
    }

    @Override
    public Category findByIdAndSalonId(Long id, Long salonId) throws Exception {
        Category category= categoryRepository.findByIdAndSalonId(id,salonId);

        if(category==null){
            throw new Exception("Category not found");
        }
        return category;
    }
}
