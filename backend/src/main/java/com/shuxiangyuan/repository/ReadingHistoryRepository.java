package com.shuxiangyuan.repository;

import com.shuxiangyuan.entity.ReadingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {

    @Query("SELECT rh FROM ReadingHistory rh LEFT JOIN FETCH rh.novel WHERE rh.userId = :userId ORDER BY rh.readAt DESC")
    List<ReadingHistory> findRecentByUserId(@Param("userId") Long userId);

    @Query("SELECT rh FROM ReadingHistory rh WHERE rh.userId = :userId ORDER BY rh.readAt DESC LIMIT 10")
    List<ReadingHistory> findTop10ByUserIdOrderByReadAtDesc(@Param("userId") Long userId);
}
