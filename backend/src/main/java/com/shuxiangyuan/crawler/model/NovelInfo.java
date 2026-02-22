package com.shuxiangyuan.crawler.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 小说信息DTO
 * 用于爬虫抓取和API返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelInfo {

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 小说标题
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 来源链接
     */
    private String sourceUrl;

    /**
     * 简介
     */
    private String description;

    /**
     * 状态 (连载/完结)
     */
    private String status;

    /**
     * 总章节数
     */
    private Integer totalChapters;
}
