package com.shuxiangyuan.repository;

import com.shuxiangyuan.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    List<Collection> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<Collection> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT c FROM Collection c LEFT JOIN FETCH c.novel LEFT JOIN FETCH c.category WHERE c.userId = :userId ORDER BY c.updatedAt DESC")
    List<Collection> findByUserIdWithDetails(@Param("userId") Long userId);

    Optional<Collection> findByUserIdAndNovelId(Long userId, Long novelId);

    @Query("SELECT c FROM Collection c WHERE c.userId = :userId ORDER BY c.rating DESC NULLS LAST")
    List<Collection> findByUserIdOrderByRatingDesc(@Param("userId") Long userId);
}
