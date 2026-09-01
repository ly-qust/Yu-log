package com.yu.blog.module.site;

import com.yu.blog.common.api.PageResult;
import com.yu.blog.module.article.service.ArticleService;
import com.yu.blog.module.article.vo.ArticleListVO;
import com.yu.blog.module.note.service.NoteService;
import com.yu.blog.module.note.vo.NoteVO;
import com.yu.blog.module.project.service.ProjectService;
import com.yu.blog.module.project.vo.ProjectVO;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SitemapController {
    private static final DateTimeFormatter XML_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ArticleService articleService;
    private final ProjectService projectService;
    private final NoteService noteService;
    private final String publicBaseUrl;

    public SitemapController(
            ArticleService articleService,
            ProjectService projectService,
            NoteService noteService,
            @Value("${yu-log.public-base-url:http://localhost}") String publicBaseUrl
    ) {
        this.articleService = articleService;
        this.projectService = projectService;
        this.noteService = noteService;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        List<String> urls = new ArrayList<>();
        addUrl(urls, "/", null);
        addUrl(urls, "/articles", null);
        addUrl(urls, "/projects", null);
        addUrl(urls, "/notes", null);
        addUrl(urls, "/about", null);
        addUrl(urls, "/timeline", null);
        addUrl(urls, "/messages", null);

        for (ArticleListVO article : loadArticles()) {
            addUrl(urls, "/articles/" + article.id(), article.updatedAt() != null ? article.updatedAt() : article.publishedAt());
        }
        for (ProjectVO project : loadProjects()) {
            addUrl(urls, "/projects/" + project.id(), project.updatedAt() != null ? project.updatedAt() : project.createdAt());
        }
        for (NoteVO note : loadNotes()) {
            addUrl(urls, "/notes/" + note.id(), note.updatedAt() != null ? note.updatedAt() : note.createdAt());
        }

        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        urls.forEach(xml::append);
        xml.append("</urlset>");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(xml.toString());
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        String body = "User-agent: *\nAllow: /\nDisallow: /admin\nDisallow: /api/admin/\nSitemap: " + publicBaseUrl + "/sitemap.xml\n";
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic()).body(body);
    }

    private List<ArticleListVO> loadArticles() {
        return loadPages((page, size) -> articleService.listPublicArticles(null, null, null, page, size, null));
    }

    private List<ProjectVO> loadProjects() {
        return loadPages((page, size) -> projectService.listPublic(null, null, null, page, size));
    }

    private List<NoteVO> loadNotes() {
        return loadPages((page, size) -> noteService.listPublic(null, null, page, size));
    }

    private <T> List<T> loadPages(PageLoader<T> loader) {
        List<T> values = new ArrayList<>();
        long page = 1;
        PageResult<T> result;
        do {
            result = loader.load(page, 100);
            values.addAll(result.list());
            page++;
        } while (result.hasNext() && page <= result.totalPages());
        return values;
    }

    private void addUrl(List<String> urls, String path, LocalDateTime modifiedAt) {
        StringBuilder item = new StringBuilder("<url><loc>")
                .append(escape(publicBaseUrl + (path.startsWith("/") ? path : "/" + path)))
                .append("</loc>");
        if (modifiedAt != null) {
            item.append("<lastmod>").append(modifiedAt.toLocalDate().format(XML_DATE)).append("</lastmod>");
        }
        item.append("</url>");
        urls.add(item.toString());
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    @FunctionalInterface
    private interface PageLoader<T> {
        PageResult<T> load(long page, long size);
    }
}
