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
 * 起点中文网 爬虫实现
 *
 * 注意：起点中文网使用 Cloudflare 保护和 JavaScript 渲染，
 * Jsoup 无法直接抓取。当前使用模拟数据用于开发演示。
 *
 * 如需实现真实抓取，请考虑：
 * 1. 使用 Selenium/Playwright 等浏览器自动化工具
 * 2. 使用专业爬虫服务（ScraperAPI、ZenRows 等）
 * 3. 申请官方 API 接口
 */
@Component
public class QidianCrawler extends AbstractCrawler {

    private static final String BASE_URL = "https://www.qidian.com";
    private static final String RANK_BASE_URL = "https://www.qidian.com/rank";

    @Override
    public String getSiteName() {
        return "qidian";
    }

    @Override
    public List<String> getSupportedRankingTypes() {
        return List.of("monthly", "click", "recommend", "new");
    }

    @Override
    protected String buildRankingUrl(String rankingType) {
        return switch (rankingType) {
            case "monthly" -> RANK_BASE_URL + "/month";          // 月票榜
            case "click" -> RANK_BASE_URL + "/click";           // 点击榜
            case "recommend" -> RANK_BASE_URL + "/recommend";    // 推荐榜
            case "new" -> RANK_BASE_URL + "/new";               // 新书榜
            default -> throw new IllegalArgumentException("起点中文网不支持的排行榜类型: " + rankingType);
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
            // 检查是否是 Cloudflare 保护页面
            String html = doc.html();
            if (html.contains("cloudflare") || html.contains("probe.js") || html.contains("buid")) {
                log.warn("检测到 Cloudflare 保护，使用模拟数据");
                return getMockData();
            }

            // 正常解析流程（如果网站没有保护）
            Elements rankItems = doc.select(".rank-list li, .rank-body .book-img-text, .rank-item");

            log.info("找到 {} 个排行榜条目", rankItems.size());

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

                if (rank > 50) {
                    break;
                }
            }

            // 如果解析失败，使用模拟数据
            if (novels.isEmpty()) {
                log.info("未能解析到数据，使用模拟数据");
                return getMockData();
            }

        } catch (Exception e) {
            log.error("解析起点排行榜失败，使用模拟数据", e);
            return getMockData();
        }

        return novels;
    }

    /**
     * 从元素中提取小说信息
     */
    private NovelInfo extractNovelInfo(Element item, int rank) {
        NovelInfo.NovelInfoBuilder builder = NovelInfo.builder();

        builder.rank(rank);

        String title = extractText(item, ".book-mid-info h4, .book-info-title a, h3 a, .book-name");
        if (title.isEmpty()) {
            title = extractText(item, "h4 a, h3 a, a[title]");
        }
        builder.title(title);

        String author = extractText(item, ".book-mid-info p span, .author, .writer-name");
        if (author.isEmpty()) {
            author = extractText(item, "p.author, span.author");
        }
        author = author.replaceAll("^[作者：:]+", "").trim();
        builder.author(author);

        String coverUrl = extractUrl(item, "img", "src");
        builder.coverUrl(coverUrl);

        String sourceUrl = extractUrl(item, "a[href*=/info/]", "href");
        builder.sourceUrl(sourceUrl);

        String description = extractText(item, ".book-mid-info p, .intro, .description");
        if (description.length() > 200) {
            description = description.substring(0, 200) + "...";
        }
        builder.description(description);

        builder.status("连载");

        return builder.build();
    }

    /**
     * 模拟数据（用于开发演示）
     * 每个榜单返回10本小说
     */
    @Override
    protected List<NovelInfo> getMockData() {
        return List.of(
            NovelInfo.builder()
                .rank(1)
                .title("完美世界")
                .author("辰东")
                .description("一粒尘可填海，一根草斩尽日月星辰，弹指间天翻地覆。")
                .sourceUrl("https://www.qidian.com/info/1010734496")
                .coverUrl("")
                .status("完结")
                .totalChapters(2000)
                .build(),
            NovelInfo.builder()
                .rank(2)
                .title("诡秘之主")
                .author("爱潜水的乌贼")
                .description("蒸汽与机械的浪潮中，谁能触及非凡？")
                .sourceUrl("https://www.qidian.com/info/1010868264")
                .coverUrl("")
                .status("完结")
                .totalChapters(1400)
                .build(),
            NovelInfo.builder()
                .rank(3)
                .title("大奉打更人")
                .author("卖报小郎君")
                .description("这个世界，有儒；有道；有佛；有妖；有术士。")
                .sourceUrl("https://www.qidian.com/info/1019665447")
                .coverUrl("")
                .status("完结")
                .totalChapters(2300)
                .build(),
            NovelInfo.builder()
                .rank(4)
                .title("深空彼岸")
                .author("辰东")
                .description("浩瀚的宇宙中，一片死寂。只有永恒的葬地。")
                .sourceUrl("https://www.qidian.com/info/1029743800")
                .coverUrl("")
                .status("连载")
                .totalChapters(800)
                .build(),
            NovelInfo.builder()
                .rank(5)
                .title("我的治愈系游戏")
                .author("我会修空调")
                .description("你要玩游戏吗？")
                .sourceUrl("https://www.qidian.com/info/1021616706")
                .coverUrl("")
                .status("连载")
                .totalChapters(600)
                .build(),
            NovelInfo.builder()
                .rank(6)
                .title("星门")
                .author("老鹰吃小鸡")
                .description("传说，在那里可以获得一切。")
                .sourceUrl("https://www.qidian.com/info/1034983746")
                .coverUrl("")
                .status("连载")
                .totalChapters(1200)
                .build(),
            NovelInfo.builder()
                .rank(7)
                .title("赤心巡天")
                .author("情何以甚")
                .description("山河千里写伏尸，乾坤百年描饿虎。")
                .sourceUrl("https://www.qidian.com/info/1035809082")
                .coverUrl("")
                .status("连载")
                .totalChapters(900)
                .build(),
            NovelInfo.builder()
                .rank(8)
                .title("长夜余火")
                .author("肘子")
                .description("余火藏于长夜，当有燃灯之人。")
                .sourceUrl("https://www.qidian.com/info/1035698006")
                .coverUrl("")
                .status("完结")
                .totalChapters(700)
                .build(),
            NovelInfo.builder()
                .rank(9)
                .title("灵境行者")
                .author(" 卖报小郎君")
                .description("灵境穿行，虚实交错，梦境与现实。")
                .sourceUrl("https://www.qidian.com/info/1038489683")
                .coverUrl("")
                .status("连载")
                .totalChapters(500)
                .build(),
            NovelInfo.builder()
                .rank(10)
                .title("明克街13号")
                .author(" 纯洁滴小龙")
                .description("这是一个关于超凡、诡秘和探案的故事。")
                .sourceUrl("https://www.qidian.com/info/1021769540")
                .coverUrl("")
                .status("连载")
                .totalChapters(400)
                .build()
        );
    }
}
