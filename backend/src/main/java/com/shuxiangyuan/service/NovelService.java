package com.shuxiangyuan.service;

import com.shuxiangyuan.entity.Novel;
import com.shuxiangyuan.repository.NovelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NovelService {

    private final NovelRepository novelRepository;

    public NovelService(NovelRepository novelRepository) {
        this.novelRepository = novelRepository;
    }

    public Page<Novel> getAllNovels(Pageable pageable) {
        return novelRepository.findAll(pageable);
    }

    public Novel getNovelById(Long id) {
        return novelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("小说不存在"));
    }

    public Novel createNovel(Novel novel) {
        return novelRepository.save(novel);
    }

    public Page<Novel> searchNovels(String title, Pageable pageable) {
        return novelRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
}
