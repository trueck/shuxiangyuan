package com.shuxiangyuan.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuxiangyuan.crawler.CrawlerFactory;
import com.shuxiangyuan.crawler.model.NovelInfo;
import com.shuxiangyuan.entity.Ranking;
import com.shuxiangyuan.repository.RankingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 排行榜业务逻辑层
 */
@Service
public class RankingService {

    private static final Logger log = LoggerFactory.getLogger(RankingService.class);

    private final CrawlerFactory crawlerFactory;
    private final RankingRepository rankingRepository;
    private final ObjectMapper objectMapper;

    public RankingService(CrawlerFactory crawlerFactory,
                          RankingRepository rankingRepository,
                          ObjectMapper objectMapper) {
        this.crawlerFactory = crawlerFactory;
        this.rankingRepository = rankingRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 抓取并保存排行榜数据
     *
     * @param siteName 网站名称
     * @param rankingType 排行榜类型
     */
    @Transactional
    public void fetchAndSaveRanking(String siteName, String rankingType) {
        log.info("开始抓取排行榜: {} - {}", siteName, rankingType);

        try {
            // 1. 获取对应的爬虫
            var crawler = crawlerFactory.getCrawler(siteName);

            // 2. 执行爬取
            List<NovelInfo> novels = crawler.crawlRanking(rankingType);

            if (novels == null || novels.isEmpty()) {
                log.warn("未获取到任何数据: {} - {}", siteName, rankingType);
                return;
            }

            // 3. 转换为JSON
            String novelsJson = objectMapper.writeValueAsString(novels);

            // 4. 查找或创建Ranking记录
            Ranking ranking = rankingRepository
                    .findBySiteNameAndRankingType(siteName, rankingType)
                    .orElse(createRanking(siteName, rankingType));

            // 5. 更新数据
            ranking.setNovels(novelsJson);
            ranking.setUpdatedAt(LocalDateTime.now());

            // 6. 保存
            rankingRepository.save(ranking);

            log.info("排行榜数据保存成功: {} - {} ({} 本小说)",
                    siteName, rankingType, novels.size());

        } catch (Exception e) {
            log.error("抓取排行榜失败: {} - {}", siteName, rankingType, e);
            throw new RuntimeException("抓取排行榜失败: " + siteName + " - " + rankingType, e);
        }
    }

    /**
     * 抓取并保存所有排行榜
     */
    @Transactional
    public void fetchAllRankings() {
        log.info("开始抓取所有排行榜数据...");

        List<String> sites = crawlerFactory.getSupportedSites();
        List<String> types = List.of("monthly", "click", "recommend");

        int successCount = 0;
        int failCount = 0;

        for (String site : sites) {
            for (String type : types) {
                try {
                    fetchAndSaveRanking(site, type);
                    successCount++;

                    // 避免请求过快
                    Thread.sleep(2000);

                } catch (Exception e) {
                    failCount++;
                    log.error("抓取失败: {} - {}", site, type);
                }
            }
        }

        log.info("排行榜数据抓取完成 - 成功: {}, 失败: {}", successCount, failCount);
    }

    /**
     * 获取排行榜数据
     *
     * @param siteName 网站名称
     * @param rankingType 排行榜类型
     * @return 排行榜数据
     */
    public RankingData getRanking(String siteName, String rankingType) {
        Ranking ranking = rankingRepository
                .findBySiteNameAndRankingType(siteName, rankingType)
                .orElseThrow(() -> new RuntimeException("排行榜数据不存在: " + siteName + " - " + rankingType));

        try {
            // 解析JSONB数据
            List<NovelInfo> novels = objectMapper.readValue(
                    ranking.getNovels(),
                    new TypeReference<List<NovelInfo>>() {}
            );

            return RankingData.builder()
                    .siteName(ranking.getSiteName())
                    .rankingType(ranking.getRankingType())
                    .title(ranking.getTitle())
                    .novels(novels)
                    .updatedAt(ranking.getUpdatedAt())
                    .build();

        } catch (Exception e) {
            log.error("解析排行榜数据失败: {} - {}", siteName, rankingType, e);
            throw new RuntimeException("解析排行榜数据失败", e);
        }
    }

    /**
     * 获取所有排行榜概览
     *
     * @return 排行榜概览列表
     */
    public List<RankingSummary> getAllRankings() {
        return rankingRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(r -> {
                    try {
                        // 解析小说数量
                        List<NovelInfo> novels = objectMapper.readValue(
                                r.getNovels(),
                                new TypeReference<List<NovelInfo>>() {}
                        );

                        return RankingSummary.builder()
                                .siteName(r.getSiteName())
                                .rankingType(r.getRankingType())
                                .title(r.getTitle())
                                .novelCount(novels.size())
                                .updatedAt(r.getUpdatedAt())
                                .build();
                    } catch (Exception e) {
                        log.warn("解析排行榜概览失败: {} - {}", r.getSiteName(), r.getRankingType());
                        return RankingSummary.builder()
                                .siteName(r.getSiteName())
                                .rankingType(r.getRankingType())
                                .title(r.getTitle())
                                .novelCount(0)
                                .updatedAt(r.getUpdatedAt())
                                .build();
                    }
                })
                .toList();
    }

    /**
     * 获取指定网站的所有排行榜
     *
     * @param siteName 网站名称
     * @return 排行榜概览列表
     */
    public List<RankingSummary> getRankingsBySite(String siteName) {
        return rankingRepository.findBySiteName(siteName)
                .stream()
                .map(r -> {
                    try {
                        List<NovelInfo> novels = objectMapper.readValue(
                                r.getNovels(),
                                new TypeReference<List<NovelInfo>>() {}
                        );

                        return RankingSummary.builder()
                                .siteName(r.getSiteName())
                                .rankingType(r.getRankingType())
                                .title(r.getTitle())
                                .novelCount(novels.size())
                                .updatedAt(r.getUpdatedAt())
                                .build();
                    } catch (Exception e) {
                        log.warn("解析排行榜概览失败: {} - {}", r.getSiteName(), r.getRankingType());
                        return RankingSummary.builder()
                                .siteName(r.getSiteName())
                                .rankingType(r.getRankingType())
                                .title(r.getTitle())
                                .novelCount(0)
                                .updatedAt(r.getUpdatedAt())
                                .build();
                    }
                })
                .toList();
    }

    /**
     * 创建新的Ranking记录
     */
    private Ranking createRanking(String siteName, String rankingType) {
        return Ranking.builder()
                .siteName(siteName)
                .rankingType(rankingType)
                .title(buildTitle(siteName, rankingType))
                .novels("[]")
                .build();
    }

    /**
     * 构建排行榜标题
     */
    private String buildTitle(String siteName, String rankingType) {
        String siteDisplayName = getSiteDisplayName(siteName);
        String typeDisplayName = getTypeDisplayName(rankingType);
        return siteDisplayName + " - " + typeDisplayName;
    }

    /**
     * 获取网站显示名称
     */
    private String getSiteDisplayName(String siteName) {
        return switch (siteName) {
            case "qidian" -> "起点中文网";
            case "zongheng" -> "纵横中文网";
            case "jjwxc" -> "晋江文学城";
            case "17k" -> "17K小说网";
            case "fanqie" -> "番茄小说";
            default -> siteName;
        };
    }

    /**
     * 获取排行榜类型显示名称
     */
    private String getTypeDisplayName(String rankingType) {
        return switch (rankingType) {
            case "monthly" -> "月票榜";
            case "click" -> "点击榜";
            case "recommend" -> "推荐榜";
            case "new" -> "新书榜";
            default -> rankingType;
        };
    }

    /**
     * 排行榜数据DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RankingData {
        private String siteName;
        private String rankingType;
        private String title;
        private List<NovelInfo> novels;
        private LocalDateTime updatedAt;
    }

    /**
     * 排行榜概览DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RankingSummary {
        private String siteName;
        private String rankingType;
        private String title;
        private Integer novelCount;
        private LocalDateTime updatedAt;
    }
}
