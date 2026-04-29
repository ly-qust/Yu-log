SET @article_reading_time_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'article'
    AND COLUMN_NAME = 'reading_time'
);

SET @article_reading_time_sql := IF(
  @article_reading_time_exists = 0,
  'ALTER TABLE `article` ADD COLUMN `reading_time` INT NOT NULL DEFAULT 1 AFTER `comment_count`',
  'SELECT 1'
);

PREPARE article_reading_time_stmt FROM @article_reading_time_sql;
EXECUTE article_reading_time_stmt;
DEALLOCATE PREPARE article_reading_time_stmt;
