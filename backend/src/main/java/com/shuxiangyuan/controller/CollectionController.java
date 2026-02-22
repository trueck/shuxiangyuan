package com.shuxiangyuan.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shuxiangyuan.dto.ApiResponse;
import com.shuxiangyuan.entity.Collection;
import com.shuxiangyuan.service.CollectionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public ApiResponse<List<Collection>> getCollections(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(collectionService.getUserCollections(userId));
    }

    @PostMapping
    public ApiResponse<Collection> addCollection(
            @RequestBody AddCollectionRequest request,
            @AuthenticationPrincipal Long userId) {
        try {
            Collection collection = collectionService.addCollection(userId, request.getNovelId(), request.getCategoryId());
            return ApiResponse.success(collection);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Collection> updateCollection(
            @PathVariable Long id,
            @RequestBody UpdateCollectionRequest request,
            @AuthenticationPrincipal Long userId) {
        try {
            Collection collection = collectionService.updateCollection(
                    id, userId, request.getRating(), request.getCategoryId(),
                    request.getReadingStatus(), request.getCurrentChapter(), request.getNotes());
            return ApiResponse.success(collection);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCollection(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        try {
            collectionService.deleteCollection(id, userId);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // DTO classes for request body
    public static class AddCollectionRequest {
        @JsonProperty("novel_id")
        private Long novelId;
        @JsonProperty("category_id")
        private Long categoryId;

        public Long getNovelId() { return novelId; }
        public void setNovelId(Long novelId) { this.novelId = novelId; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    }

    public static class UpdateCollectionRequest {
        private Integer rating;
        @JsonProperty("category_id")
        private Long categoryId;
        @JsonProperty("reading_status")
        private String readingStatus;
        @JsonProperty("current_chapter")
        private Integer currentChapter;
        private String notes;

        public java.math.BigDecimal getRating() {
            return rating != null ? java.math.BigDecimal.valueOf(rating) : null;
        }
        public void setRating(Integer rating) { this.rating = rating; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getReadingStatus() { return readingStatus; }
        public void setReadingStatus(String readingStatus) { this.readingStatus = readingStatus; }
        public Integer getCurrentChapter() { return currentChapter; }
        public void setCurrentChapter(Integer currentChapter) { this.currentChapter = currentChapter; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
