package com.shuxiangyuan.repository;

import com.shuxiangyuan.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndName(Long userId, String name);
}
