package com.shuxiangyuan.repository;

import com.shuxiangyuan.entity.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 排行榜数据访问层
 */
@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {

    /**
     * 根据网站名称和排行榜类型查询
     */
    Optional<Ranking> findBySiteNameAndRankingType(String siteName, String rankingType);

    /**
     * 查询指定网站的所有排行榜
     */
    List<Ranking> findBySiteName(String siteName);

    /**
     * 查询所有排行榜，按更新时间倒序
     */
    List<Ranking> findAllByOrderByUpdatedAtDesc();

    /**
     * 查询指定网站的指定类型的排行榜是否存在
     */
    boolean existsBySiteNameAndRankingType(String siteName, String rankingType);
}
