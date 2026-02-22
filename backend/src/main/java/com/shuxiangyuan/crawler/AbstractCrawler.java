package com.shuxiangyuan.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuxiangyuan.crawler.model.NovelInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * 抽象爬虫基类
 * 实现通用的爬取流程和反爬虫策略
 */
public abstract class AbstractCrawler implements Crawler {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final Random random = new Random();
    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 预定义的User-Agent列表
     */
    protected static final List<String> USER_AGENTS = List.of(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edge/120.0.0.0",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1"
    );

    /**
     * 爬取排行榜数据（模板方法）
     */
    @Override
    public List<NovelInfo> crawlRanking(String rankingType) {
        log.info("开始爬取 {} 排行榜: {}", getSiteName(), rankingType);

        try {
            // 1. 构建URL
            String url = buildRankingUrl(rankingType);
            log.debug("请求URL: {}", url);

            // 2. 发送HTTP请求（包含重试逻辑）
            Document doc = fetchDocumentWithRetry(url, 3);

            // 3. 解析HTML
            List<NovelInfo> novels = parseRankingDocument(doc);

            log.info("爬取完成，共获取 {} 本小说", novels.size());
            return novels;

        } catch (Exception e) {
            log.error("爬取失败: {} - {}，尝试使用模拟数据", getSiteName(), rankingType, e);
            // 尝试使用模拟数据
            List<NovelInfo> mockData = getMockData();
            if (mockData != null && !mockData.isEmpty()) {
                log.info("使用模拟数据成功，共 {} 本小说", mockData.size());
                return mockData;
            }
            // 如果没有模拟数据，抛出异常
            throw new RuntimeException("爬取失败: " + getSiteName() + " - " + rankingType, e);
        }
    }

    /**
     * 获取模拟数据（子类可重写此方法提供模拟数据）
     * @return 模拟数据列表，如果没有则返回空列表
     */
    protected List<NovelInfo> getMockData() {
        return List.of();
    }

    /**
     * 发送HTTP请求并获取文档（带重试机制）
     */
    protected Document fetchDocumentWithRetry(String url, int maxRetries) throws IOException {
        int retryCount = 0;
        IOException lastException = null;

        while (retryCount < maxRetries) {
            try {
                // 添加随机延迟避免请求过快
                if (retryCount > 0) {
                    long delay = (long) (Math.pow(2, retryCount) * 1000 + random.nextInt(2000));
                    log.info("重试第 {} 次，延迟 {} ms", retryCount, delay);
                    Thread.sleep(delay);
                }

                return fetchDocument(url);

            } catch (IOException e) {
                lastException = e;
                retryCount++;
                log.warn("请求失败，准备重试: {} ({}/{})", url, retryCount, maxRetries);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("爬取被中断", e);
            }
        }

        throw new IOException("请求失败，已重试 " + maxRetries + " 次: " + url, lastException);
    }

    /**
     * 发送HTTP请求并获取文档
     */
    protected Document fetchDocument(String url) throws IOException {
        // 随机选择User-Agent
        String userAgent = USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));

        log.info("正在请求 URL: {}", url);

        var connection = org.jsoup.Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(15000)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Connection", "keep-alive")
                .followRedirects(true)
                .maxBodySize(10 * 1024 * 1024); // 10MB

        Document doc = connection.get();

        log.info("请求完成 - 状态: {}, 标题: {}, body长度: {}",
                connection.response().statusCode(),
                doc.title(),
                doc.body() != null ? doc.body().html().length() : 0);

        // 如果页面为空，打印整个文档用于调试
        if (doc.body() == null || doc.body().html().length() < 100) {
            log.warn("页面内容为空或过短! 完整HTML: {}", doc.html());
        }

        return doc;
    }

    /**
     * 从元素中提取文本内容，去除空白字符
     */
    protected String extractText(Element element, String selector) {
        Elements els = element.select(selector);
        return els.isEmpty() ? "" : els.first().text().trim();
    }

    /**
     * 从元素中提取属性值
     */
    protected String extractAttr(Element element, String selector, String attr) {
        Elements els = element.select(selector);
        return els.isEmpty() ? "" : els.first().attr(attr).trim();
    }

    /**
     * 从元素中提取链接（处理相对路径）
     */
    protected String extractUrl(Element element, String selector, String attr) {
        String url = extractAttr(element, selector, attr);
        if (url.isEmpty()) {
            return "";
        }

        // 处理相对路径
        if (url.startsWith("//")) {
            return "https:" + url;
        } else if (url.startsWith("/")) {
            return getBaseUrl() + url;
        } else if (!url.startsWith("http")) {
            return getBaseUrl() + "/" + url;
        }

        return url;
    }

    // =============== 抽象方法，由子类实现 ===============

    /**
     * 构建排行榜URL
     *
     * @param rankingType 排行榜类型
     * @return 完整的URL
     */
    protected abstract String buildRankingUrl(String rankingType);

    /**
     * 解析排行榜HTML文档
     *
     * @param doc HTML文档
     * @return 小说信息列表
     */
    protected abstract List<NovelInfo> parseRankingDocument(Document doc);

    /**
     * 获取网站基础URL（用于处理相对路径）
     */
    protected abstract String getBaseUrl();
}
