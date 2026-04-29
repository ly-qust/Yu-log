SET @comment_admin_reply_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'comment'
    AND COLUMN_NAME = 'admin_reply'
);

SET @comment_admin_reply_sql := IF(
  @comment_admin_reply_exists = 0,
  'ALTER TABLE `comment` ADD COLUMN `admin_reply` TEXT DEFAULT NULL AFTER `status`',
  'SELECT 1'
);

PREPARE comment_admin_reply_stmt FROM @comment_admin_reply_sql;
EXECUTE comment_admin_reply_stmt;
DEALLOCATE PREPARE comment_admin_reply_stmt;

SET @comment_replied_at_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'comment'
    AND COLUMN_NAME = 'replied_at'
);

SET @comment_replied_at_sql := IF(
  @comment_replied_at_exists = 0,
  'ALTER TABLE `comment` ADD COLUMN `replied_at` DATETIME DEFAULT NULL AFTER `admin_reply`',
  'SELECT 1'
);

PREPARE comment_replied_at_stmt FROM @comment_replied_at_sql;
EXECUTE comment_replied_at_stmt;
DEALLOCATE PREPARE comment_replied_at_stmt;

SET @message_replied_at_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'message'
    AND COLUMN_NAME = 'replied_at'
);

SET @message_replied_at_sql := IF(
  @message_replied_at_exists = 0,
  'ALTER TABLE `message` ADD COLUMN `replied_at` DATETIME DEFAULT NULL AFTER `reply_user_id`',
  'SELECT 1'
);

PREPARE message_replied_at_stmt FROM @message_replied_at_sql;
EXECUTE message_replied_at_stmt;
DEALLOCATE PREPARE message_replied_at_stmt;
