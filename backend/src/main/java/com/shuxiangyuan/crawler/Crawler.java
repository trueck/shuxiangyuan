package com.shuxiangyuan.crawler;

import com.shuxiangyuan.crawler.model.NovelInfo;

import java.util.List;

/**
 * 爬虫接口
 * 所有小说网站爬虫都需要实现此接口
 */
public interface Crawler {

    /**
     * 爬取排行榜数据
     *
     * @param rankingType 排行榜类型 (monthly: 月票榜, click: 点击榜, recommend: 推荐榜)
     * @return 小说信息列表
     */
    List<NovelInfo> crawlRanking(String rankingType);

    /**
     * 获取网站名称
     *
     * @return 网站名称标识
     */
    String getSiteName();

    /**
     * 获取支持的排行榜类型
     *
     * @return 支持的排行榜类型列表
     */
    List<String> getSupportedRankingTypes();
}
