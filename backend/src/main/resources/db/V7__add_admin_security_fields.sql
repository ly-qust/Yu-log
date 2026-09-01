SET @must_change_password_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND COLUMN_NAME = 'must_change_password'
);

SET @must_change_password_sql := IF(
  @must_change_password_exists = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `must_change_password` TINYINT NOT NULL DEFAULT 0 AFTER `status`',
  'SELECT 1'
);
PREPARE must_change_password_stmt FROM @must_change_password_sql;
EXECUTE must_change_password_stmt;
DEALLOCATE PREPARE must_change_password_stmt;

UPDATE `sys_user`
SET `must_change_password` = 1
WHERE `role_code` = 'ADMIN'
  AND `deleted` = 0;
