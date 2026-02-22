package com.shuxiangyuan.controller;

import com.shuxiangyuan.dto.ApiResponse;
import com.shuxiangyuan.entity.Category;
import com.shuxiangyuan.service.CategoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<Category>> getCategories(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(categoryService.getUserCategories(userId));
    }

    @PostMapping
    public ApiResponse<Category> createCategory(@RequestBody Category category, @AuthenticationPrincipal Long userId) {
        try {
            category.setUserId(userId);
            return ApiResponse.success(categoryService.createCategory(category));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> updateCategory(@PathVariable Long id, @RequestBody Category category, @AuthenticationPrincipal Long userId) {
        try {
            category.setUserId(userId);
            return ApiResponse.success(categoryService.updateCategory(id, category));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(null);
    }
}
