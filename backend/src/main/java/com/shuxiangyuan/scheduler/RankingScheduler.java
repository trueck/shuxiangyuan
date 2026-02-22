package com.shuxiangyuan.scheduler;

import com.shuxiangyuan.service.RankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 排行榜定时任务调度器
 * 定时自动抓取各网站排行榜数据
 */
@Component
public class RankingScheduler {

    private static final Logger log = LoggerFactory.getLogger(RankingScheduler.class);

    private final RankingService rankingService;

    public RankingScheduler(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * 每天凌晨2点抓取所有网站的所有排行榜
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void fetchAllRankingsDaily() {
        log.info("=== 开始执行每日排行榜数据抓取任务 ===");

        try {
            rankingService.fetchAllRankings();
            log.info("=== 每日排行榜数据抓取任务完成 ===");
        } catch (Exception e) {
            log.error("每日排行榜数据抓取任务失败", e);
        }
    }

    /**
     * 每小时更新热门榜单（点击榜）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void fetchHotRankingHourly() {
        log.info("=== 开始执行每小时热门榜单更新任务 ===");

        try {
            // 只抓取最热门的榜单（点击榜）
            String[] sites = {"qidian", "zongheng", "jjwxc", "17k", "fanqie"};

            int successCount = 0;
            int failCount = 0;

            for (String site : sites) {
                try {
                    rankingService.fetchAndSaveRanking(site, "click");
                    successCount++;

                    // 避免请求过快，每个请求间隔2秒
                    Thread.sleep(2000);

                } catch (Exception e) {
                    failCount++;
                    log.error("抓取失败: {} - click", site, e);
                }
            }

            log.info("=== 每小时热门榜单更新任务完成 - 成功: {}, 失败: {} ===", successCount, failCount);

        } catch (Exception e) {
            log.error("每小时热门榜单更新任务失败", e);
        }
    }

    /**
     * 每30分钟检查并更新起点月票榜（最热门榜单）
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void fetchQidianMonthlyRanking() {
        log.info("=== 开始执行起点月票榜更新任务 ===");

        try {
            rankingService.fetchAndSaveRanking("qidian", "monthly");
            log.info("=== 起点月票榜更新任务完成 ===");
        } catch (Exception e) {
            log.error("起点月票榜更新任务失败", e);
        }
    }

    /**
     * 测试方法：手动触发一次完整的排行榜抓取
     * 可通过管理后台API或开发工具调用
     */
    public void manualFetchAll() {
        log.info("=== 手动触发：抓取所有排行榜 ===");

        try {
            rankingService.fetchAllRankings();
            log.info("=== 手动触发：排行榜抓取完成 ===");
        } catch (Exception e) {
            log.error("手动触发排行榜抓取失败", e);
        }
    }
}
