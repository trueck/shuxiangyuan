package com.shuxiangyuan.service;

import com.shuxiangyuan.entity.Category;
import com.shuxiangyuan.entity.Collection;
import com.shuxiangyuan.entity.Novel;
import com.shuxiangyuan.repository.CategoryRepository;
import com.shuxiangyuan.repository.CollectionRepository;
import com.shuxiangyuan.repository.NovelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final NovelRepository novelRepository;
    private final CategoryRepository categoryRepository;

    public CollectionService(CollectionRepository collectionRepository,
                            NovelRepository novelRepository,
                            CategoryRepository categoryRepository) {
        this.collectionRepository = collectionRepository;
        this.novelRepository = novelRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Collection> getUserCollections(Long userId) {
        return collectionRepository.findByUserIdWithDetails(userId);
    }

    public List<Collection> getUserCollectionsByCategory(Long userId, Long categoryId) {
        return collectionRepository.findByUserIdAndCategoryId(userId, categoryId);
    }

    public List<Collection> getUserCollectionsByRating(Long userId) {
        return collectionRepository.findByUserIdOrderByRatingDesc(userId);
    }

    @Transactional
    public Collection addCollection(Long userId, Long novelId, Long categoryId) {
        // 检查是否已收藏
        Optional<Collection> existing = collectionRepository.findByUserIdAndNovelId(userId, novelId);
        if (existing.isPresent()) {
            throw new RuntimeException("已经收藏过该小说");
        }

        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在"));

        Collection collection = new Collection();
        collection.setUserId(userId);
        collection.setNovel(novel);
        collection.setNovelId(novelId);

        // 如果有分类，设置分类实体
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElse(null);
            collection.setCategory(category);
            collection.setCategoryId(categoryId);
        }

        collection.setReadingStatus("reading");

        return collectionRepository.save(collection);
    }

    @Transactional
    public Collection updateCollection(Long id, Long userId, BigDecimal rating, Long categoryId,
                                       String readingStatus, Integer currentChapter, String notes) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("收藏不存在"));

        // 验证权限
        if (!collection.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        if (rating != null) {
            collection.setRating(rating);
        }
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            collection.setCategory(category);
            collection.setCategoryId(categoryId);
        }
        if (readingStatus != null) {
            collection.setReadingStatus(readingStatus);
        }
        if (currentChapter != null) {
            collection.setCurrentChapter(currentChapter);
        }
        if (notes != null) {
            collection.setNotes(notes);
        }

        return collectionRepository.save(collection);
    }

    public void deleteCollection(Long id, Long userId) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("收藏不存在"));

        if (!collection.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        collectionRepository.delete(collection);
    }
}
