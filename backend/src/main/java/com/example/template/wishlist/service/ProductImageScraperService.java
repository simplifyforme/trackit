package com.example.template.wishlist.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductImageScraperService {

    private static final int TIMEOUT_MS = 8_000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Safari/537.36";

    /**
     * Fetches the product page and extracts the best available image URL.
     * Tries og:image → twitter:image → first prominent img src.
     * Returns null if the page cannot be reached or no image is found.
     */
    public String scrape(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            String image = ogImage(doc);
            if (image != null) return image;

            image = metaContent(doc, "twitter:image");
            if (image != null) return image;

            image = firstProductImage(doc);
            return image;

        } catch (Exception e) {
            log.warn("Could not scrape image from {}: {}", url, e.getMessage());
            return null;
        }
    }

    private String ogImage(Document doc) {
        return metaContent(doc, "og:image");
    }

    private String metaContent(Document doc, String property) {
        Element el = doc.selectFirst("meta[property=" + property + "]");
        if (el == null) el = doc.selectFirst("meta[name=" + property + "]");
        if (el == null) return null;
        String content = el.attr("content");
        return content.isBlank() ? null : content;
    }

    private String firstProductImage(Document doc) {
        // Prefer images with product-related class/id hints, then fallback to largest src
        for (Element img : doc.select("img[src]")) {
            String src = img.attr("abs:src");
            if (src.isBlank()) continue;
            String alt = img.attr("alt").toLowerCase();
            String cls = img.attr("class").toLowerCase();
            if (alt.contains("product") || cls.contains("product") || cls.contains("main-image")) {
                return src;
            }
        }
        // Last resort: first non-tiny image
        for (Element img : doc.select("img[src]")) {
            String src = img.attr("abs:src");
            if (!src.isBlank() && !src.contains("logo") && !src.contains("icon")) {
                return src;
            }
        }
        return null;
    }
}
