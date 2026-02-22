package com.shuxiangyuan.controller;

import com.shuxiangyuan.dto.ApiResponse;
import com.shuxiangyuan.service.RankingService;
import com.shuxiangyuan.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排行榜API控制器
 */
@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * 获取所有排行榜概览（公开API）
     */
    @GetMapping
    public ApiResponse<List<RankingService.RankingSummary>> getAllRankings() {
        return ApiResponse.success(rankingService.getAllRankings());
    }

    /**
     * 获取指定网站的排行榜概览（公开API）
     */
    @GetMapping("/{siteName}")
    public ApiResponse<List<RankingService.RankingSummary>> getRankingsBySite(
            @PathVariable String siteName) {
        return ApiResponse.success(rankingService.getRankingsBySite(siteName));
    }

    /**
     * 获取特定排行榜数据（公开API）
     *
     * @param siteName 网站名称 (qidian, zongheng, jjwxc, 17k, fanqie)
     * @param rankingType 排行榜类型 (monthly, click, recommend, new)
     */
    @GetMapping("/{siteName}/{rankingType}")
    public ApiResponse<RankingService.RankingData> getRanking(
            @PathVariable String siteName,
            @PathVariable String rankingType) {
        return ApiResponse.success(rankingService.getRanking(siteName, rankingType));
    }

    /**
     * 手动触发爬虫抓取（需要认证）
     */
    @PostMapping("/fetch/{siteName}/{rankingType}")
    public ApiResponse<Void> fetchRanking(
            @PathVariable String siteName,
            @PathVariable String rankingType,
            @AuthenticationPrincipal Long userId) {
        System.out.println("=== fetchRanking called with siteName=" + siteName + ", rankingType=" + rankingType + ", userId=" + userId + " ===");
        rankingService.fetchAndSaveRanking(siteName, rankingType);
        return ApiResponse.success(null);
    }

    /**
     * 手动触发抓取所有排行榜（需要认证，管理员功能）
     */
    @PostMapping("/fetch-all")
    public ApiResponse<Void> fetchAllRankings(@AuthenticationPrincipal Long userId) {
        rankingService.fetchAllRankings();
        return ApiResponse.success(null);
    }
}
