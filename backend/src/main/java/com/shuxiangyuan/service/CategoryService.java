package com.shuxiangyuan.service;

import com.shuxiangyuan.entity.Category;
import com.shuxiangyuan.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getUserCategories(Long userId) {
        return categoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByUserIdAndName(category.getUserId(), category.getName())) {
            throw new RuntimeException("分类名称已存在");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        if (!existing.getName().equals(category.getName()) &&
                categoryRepository.existsByUserIdAndName(category.getUserId(), category.getName())) {
            throw new RuntimeException("分类名称已存在");
        }

        existing.setName(category.getName());
        existing.setColor(category.getColor());
        existing.setIcon(category.getIcon());

        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
