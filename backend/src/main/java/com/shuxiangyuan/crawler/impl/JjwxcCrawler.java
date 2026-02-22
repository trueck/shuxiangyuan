package com.shuxiangyuan.crawler.impl;

import com.shuxiangyuan.crawler.AbstractCrawler;
import com.shuxiangyuan.crawler.model.NovelInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 晋江文学城 爬虫实现
 */
@Component
public class JjwxcCrawler extends AbstractCrawler {

    private static final String BASE_URL = "https://www.jjwxc.net";
    private static final String RANK_BASE_URL = "https://www.jjwxc.net/rank";

    @Override
    public String getSiteName() {
        return "jjwxc";
    }

    @Override
    public List<String> getSupportedRankingTypes() {
        return List.of("monthly", "click", "recommend");
    }

    @Override
    protected String buildRankingUrl(String rankingType) {
        return switch (rankingType) {
            case "monthly" -> RANK_BASE_URL + "/month";          // 月票榜
            case "click" -> RANK_BASE_URL + "/click";           // 点击榜
            case "recommend" -> RANK_BASE_URL + "/recommend";    // 推荐榜
            default -> throw new IllegalArgumentException("晋江文学城不支持的排行榜类型: " + rankingType);
        };
    }

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    protected List<NovelInfo> parseRankingDocument(Document doc) {
        List<NovelInfo> novels = new ArrayList<>();

        try {
            Elements rankItems = doc.select(".rank-list li, .rank-item, .novel-item");

            log.debug("晋江找到 {} 个排行榜条目", rankItems.size());

            int rank = 1;
            for (Element item : rankItems) {
                try {
                    NovelInfo novel = extractNovelInfo(item, rank);
                    if (novel != null && novel.getTitle() != null && !novel.getTitle().isEmpty()) {
                        novels.add(novel);
                        rank++;
                    }
                } catch (Exception e) {
                    log.warn("解析小说信息失败 (rank {}): {}", rank, e.getMessage());
                }

                if (rank > 50) break;
            }

            if (novels.isEmpty()) {
                novels = tryAlternateSelectors(doc);
            }

        } catch (Exception e) {
            log.error("解析晋江排行榜失败", e);
        }

        return novels;
    }

    private NovelInfo extractNovelInfo(Element item, int rank) {
        NovelInfo.NovelInfoBuilder builder = NovelInfo.builder();
        builder.rank(rank);

        String title = extractText(item, ".novel-name a, h3 a, h4 a, .title");
        builder.title(title);

        String author = extractText(item, ".author, .writer, span.author");
        author = author.replaceAll("^[作者：:]+", "").trim();
        builder.author(author);

        String coverUrl = extractUrl(item, "img", "src");
        builder.coverUrl(coverUrl);

        String sourceUrl = extractUrl(item, "a[href*=/onebook/]", "href");
        builder.sourceUrl(sourceUrl);

        String description = extractText(item, ".intro, .description, p.intro");
        if (description.length() > 200) {
            description = description.substring(0, 200) + "...";
        }
        builder.description(description);

        builder.status("连载");

        return builder.build();
    }

    private List<NovelInfo> tryAlternateSelectors(Document doc) {
        List<NovelInfo> novels = new ArrayList<>();
        log.info("使用备用选择器解析晋江排行榜");

        Elements links = doc.select("a[href*=/onebook/]");
        int rank = 1;

        for (Element link : links) {
            if (rank > 50) break;

            String title = link.text().trim();
            String sourceUrl = extractAttr(link, "", "abs:href");

            if (!title.isEmpty() && !sourceUrl.isEmpty()) {
                novels.add(NovelInfo.builder()
                        .rank(rank)
                        .title(title)
                        .sourceUrl(sourceUrl)
                        .author("")
                        .status("连载")
                        .build());
                rank++;
            }
        }

        return novels;
    }
}
