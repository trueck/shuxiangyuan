package com.shuxiangyuan.repository;

import com.shuxiangyuan.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {

    Page<Novel> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT n FROM Novel n WHERE n.sourceSite = :site")
    Page<Novel> findBySourceSite(@Param("site") String site, Pageable pageable);

    boolean existsBySourceUrl(String sourceUrl);
}
