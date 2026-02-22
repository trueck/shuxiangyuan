package com.shuxiangyuan.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 爬虫工厂类
 * 根据网站名称创建对应的爬虫实例
 */
@Component
public class CrawlerFactory {

    private static final Logger log = LoggerFactory.getLogger(CrawlerFactory.class);

    /**
     * 所有爬虫实例的映射
     * Spring会自动将所有Crawler实现类注入到这里
     */
    private final Map<String, Crawler> crawlerMap;

    public CrawlerFactory(List<Crawler> crawlers) {
        this.crawlerMap = crawlers.stream()
                .collect(Collectors.toMap(
                        Crawler::getSiteName,
                        Function.identity()
                ));

        log.info("爬虫工厂初始化完成，支持的网站: {}", crawlerMap.keySet());
    }

    /**
     * 根据网站名称获取爬虫实例
     *
     * @param siteName 网站名称
     * @return 爬虫实例
     * @throws IllegalArgumentException 如果网站不支持
     */
    public Crawler getCrawler(String siteName) {
        Crawler crawler = crawlerMap.get(siteName);
        if (crawler == null) {
            throw new IllegalArgumentException("不支持的网站: " + siteName + "，支持的网站: " + crawlerMap.keySet());
        }
        return crawler;
    }

    /**
     * 获取所有支持的网站名称
     *
     * @return 网站名称列表
     */
    public List<String> getSupportedSites() {
        return List.copyOf(crawlerMap.keySet());
    }

    /**
     * 检查是否支持指定网站
     *
     * @param siteName 网站名称
     * @return 是否支持
     */
    public boolean isSupported(String siteName) {
        return crawlerMap.containsKey(siteName);
    }
}
