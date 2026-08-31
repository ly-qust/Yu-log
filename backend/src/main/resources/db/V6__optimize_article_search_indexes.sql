SET @idx_article_status_top_published_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'article'
    AND INDEX_NAME = 'idx_article_status_top_published'
);

SET @idx_article_status_top_published_sql := IF(
  @idx_article_status_top_published_exists = 0,
  'CREATE INDEX `idx_article_status_top_published` ON `article` (`status`, `is_top`, `published_at`, `id`)',
  'SELECT 1'
);
PREPARE idx_article_status_top_published_stmt FROM @idx_article_status_top_published_sql;
EXECUTE idx_article_status_top_published_stmt;
DEALLOCATE PREPARE idx_article_status_top_published_stmt;

SET @idx_article_category_status_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'article'
    AND INDEX_NAME = 'idx_article_category_status'
);

SET @idx_article_category_status_sql := IF(
  @idx_article_category_status_exists = 0,
  'CREATE INDEX `idx_article_category_status` ON `article` (`category_id`, `status`, `published_at`)',
  'SELECT 1'
);
PREPARE idx_article_category_status_stmt FROM @idx_article_category_status_sql;
EXECUTE idx_article_category_status_stmt;
DEALLOCATE PREPARE idx_article_category_status_stmt;

SET @idx_article_title_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'article'
    AND INDEX_NAME = 'idx_article_title'
);

SET @idx_article_title_sql := IF(
  @idx_article_title_exists = 0,
  'CREATE INDEX `idx_article_title` ON `article` (`title`)',
  'SELECT 1'
);
PREPARE idx_article_title_stmt FROM @idx_article_title_sql;
EXECUTE idx_article_title_stmt;
DEALLOCATE PREPARE idx_article_title_stmt;

SET @idx_article_summary_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'article'
    AND INDEX_NAME = 'idx_article_summary'
);

SET @idx_article_summary_sql := IF(
  @idx_article_summary_exists = 0,
  'CREATE INDEX `idx_article_summary` ON `article` (`summary`(191))',
  'SELECT 1'
);
PREPARE idx_article_summary_stmt FROM @idx_article_summary_sql;
EXECUTE idx_article_summary_stmt;
DEALLOCATE PREPARE idx_article_summary_stmt;
