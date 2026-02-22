package com.shuxiangyuan.controller;

import com.shuxiangyuan.dto.ApiResponse;
import com.shuxiangyuan.entity.Novel;
import com.shuxiangyuan.service.NovelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/novels")
public class NovelController {

    private final NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @GetMapping
    public ApiResponse<Page<Novel>> getNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ApiResponse.success(novelService.getAllNovels(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<Novel> getNovel(@PathVariable Long id) {
        try {
            return ApiResponse.success(novelService.getNovelById(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<Novel> createNovel(@RequestBody Novel novel) {
        try {
            return ApiResponse.success(novelService.createNovel(novel));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ApiResponse<Page<Novel>> searchNovels(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(novelService.searchNovels(title, pageable));
    }
}
