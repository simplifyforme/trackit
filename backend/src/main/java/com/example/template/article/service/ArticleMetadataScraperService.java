package com.example.template.article.service;

import com.example.template.article.dto.ArticleMetadataResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ArticleMetadataScraperService {

    private static final int TIMEOUT_MS = 8_000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Safari/537.36";

    /**
     * Fetches a news/article page and extracts the best available title and
     * lead image, using the same Open Graph conventions most publishers (incl.
     * paywalled ones, since these tags are rendered for social-share previews)
     * expose. Falls back gracefully to nulls per field rather than failing.
     */
    public ArticleMetadataResponse scrape(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            return new ArticleMetadataResponse(extractTitle(doc), extractImage(doc));
        } catch (Exception e) {
            log.warn("Could not fetch article metadata from {}: {}", url, e.getMessage());
            return new ArticleMetadataResponse(null, null);
        }
    }

    private String extractTitle(Document doc) {
        String title = metaContent(doc, "og:title");
        if (title != null) return title;

        title = metaContent(doc, "twitter:title");
        if (title != null) return title;

        String docTitle = doc.title();
        return docTitle.isBlank() ? null : docTitle;
    }

    private String extractImage(Document doc) {
        String image = metaContent(doc, "og:image");
        if (image != null) return image;

        image = metaContent(doc, "twitter:image");
        if (image != null) return image;

        return firstProminentImage(doc);
    }

    private String metaContent(Document doc, String property) {
        Element el = doc.selectFirst("meta[property=" + property + "]");
        if (el == null) el = doc.selectFirst("meta[name=" + property + "]");
        if (el == null) return null;
        String content = el.attr("content");
        return content.isBlank() ? null : content;
    }

    private String firstProminentImage(Document doc) {
        for (Element img : doc.select("img[src]")) {
            String src = img.attr("abs:src");
            if (src.isBlank()) continue;
            String alt = img.attr("alt").toLowerCase();
            String cls = img.attr("class").toLowerCase();
            if (alt.contains("article") || alt.contains("hero") || cls.contains("hero") || cls.contains("featured")) {
                return src;
            }
        }
        for (Element img : doc.select("img[src]")) {
            String src = img.attr("abs:src");
            if (!src.isBlank() && !src.contains("logo") && !src.contains("icon")) {
                return src;
            }
        }
        return null;
    }
}
