package com.victusstore.controller;

import com.victusstore.model.Category;
import com.victusstore.model.Product;
import com.victusstore.repository.CategoryRepository;
import com.victusstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serves dynamic robots.txt and sitemaps for SEO.
 * URLs are generated from the configured base URL and live product/category data.
 */
@RestController
public class SitemapController {

    private static final DateTimeFormatter SITEMAP_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String SITEMAP_NS = "http://www.sitemaps.org/schemas/sitemap/0.9";

    @Value("${app.store.base-url}")
    private String baseUrl;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public SitemapController(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        String content = """
            User-agent: *
            Allow: /

            Sitemap: %s/sitemap.xml
            """.formatted(baseUrl.trim().replaceAll("/$", ""));
        return ResponseEntity.ok(content);
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemapIndex() {
        String today = LocalDateTime.now().format(SITEMAP_DATE);
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <sitemapindex xmlns="%s">
                <sitemap>
                    <loc>%s/sitemaps/pages.xml</loc>
                    <lastmod>%s</lastmod>
                </sitemap>
                <sitemap>
                    <loc>%s/sitemaps/products.xml</loc>
                    <lastmod>%s</lastmod>
                </sitemap>
                <sitemap>
                    <loc>%s/sitemaps/categories.xml</loc>
                    <lastmod>%s</lastmod>
                </sitemap>
            </sitemapindex>
            """.formatted(SITEMAP_NS, baseUrl, today, baseUrl, today, baseUrl, today);
        return ResponseEntity.ok(xml);
    }

    @GetMapping(value = "/sitemaps/pages.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> pagesSitemap() {
        String today = LocalDateTime.now().format(SITEMAP_DATE);
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="%s">
                <url>
                    <loc>%s/</loc>
                    <lastmod>%s</lastmod>
                    <changefreq>daily</changefreq>
                    <priority>1.0</priority>
                </url>
                <url>
                    <loc>%s/products</loc>
                    <lastmod>%s</lastmod>
                    <changefreq>daily</changefreq>
                    <priority>0.9</priority>
                </url>
                <url>
                    <loc>%s/categories</loc>
                    <lastmod>%s</lastmod>
                    <changefreq>weekly</changefreq>
                    <priority>0.8</priority>
                </url>
            </urlset>
            """.formatted(SITEMAP_NS, baseUrl, today, baseUrl, today, baseUrl, today);
        return ResponseEntity.ok(xml);
    }

    @GetMapping(value = "/sitemaps/products.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> productsSitemap() {
        List<Product> products = productRepository.findByIsActiveTrue();

        String urlEntries = products.stream()
                .map(p -> {
                    LocalDateTime date = p.getUpdatedAt() != null ? p.getUpdatedAt() : p.getCreatedAt();
                    String lastmod = (date != null ? date : LocalDateTime.now()).format(SITEMAP_DATE);
                    return """
                        <url>
                            <loc>%s/products/%d</loc>
                            <lastmod>%s</lastmod>
                            <changefreq>weekly</changefreq>
                            <priority>0.7</priority>
                        </url>
                        """.formatted(baseUrl, p.getProductId(), lastmod);
                })
                .collect(Collectors.joining());

        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="%s">
            %s
            </urlset>
            """.formatted(SITEMAP_NS, urlEntries);

        return ResponseEntity.ok(xml);
    }

    @GetMapping(value = "/sitemaps/categories.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> categoriesSitemap() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();

        String urlEntries = categories.stream()
                .map(c -> {
                    String lastmod = c.getCreatedAt() != null
                            ? c.getCreatedAt().format(SITEMAP_DATE)
                            : LocalDateTime.now().format(SITEMAP_DATE);
                    return """
                        <url>
                            <loc>%s/categories/%d</loc>
                            <lastmod>%s</lastmod>
                            <changefreq>weekly</changefreq>
                            <priority>0.7</priority>
                        </url>
                        """.formatted(baseUrl, c.getCategoryId(), lastmod);
                })
                .collect(Collectors.joining());

        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="%s">
            %s
            </urlset>
            """.formatted(SITEMAP_NS, urlEntries);

        return ResponseEntity.ok(xml);
    }
}
