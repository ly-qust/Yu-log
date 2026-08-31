package com.yu.blog.module.article.task;

import com.yu.blog.module.article.service.ArticleCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCounterSyncTask {
    private final ArticleCounterService articleCounterService;

    @Scheduled(
            initialDelayString = "${yu-log.article-counter.initial-delay-ms:60000}",
            fixedDelayString = "${yu-log.article-counter.sync-delay-ms:60000}"
    )
    public void sync() {
        try {
            articleCounterService.syncDeltasToDatabase();
        } catch (Exception ex) {
            log.warn("Scheduled article counter sync failed", ex);
        }
    }
}
