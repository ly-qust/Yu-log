SET @project_detail_content_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'project'
    AND COLUMN_NAME = 'detail_content'
);

SET @project_detail_content_sql := IF(
  @project_detail_content_exists = 0,
  'ALTER TABLE `project` ADD COLUMN `detail_content` TEXT DEFAULT NULL AFTER `description`',
  'SELECT 1'
);
PREPARE project_detail_content_stmt FROM @project_detail_content_sql;
EXECUTE project_detail_content_stmt;
DEALLOCATE PREPARE project_detail_content_stmt;

SET @project_visible_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'project'
    AND COLUMN_NAME = 'visible'
);

SET @project_visible_sql := IF(
  @project_visible_exists = 0,
  'ALTER TABLE `project` ADD COLUMN `visible` TINYINT NOT NULL DEFAULT 1 AFTER `is_featured`',
  'SELECT 1'
);
PREPARE project_visible_stmt FROM @project_visible_sql;
EXECUTE project_visible_stmt;
DEALLOCATE PREPARE project_visible_stmt;

UPDATE `project`
SET `detail_content` = `learning_summary`
WHERE `detail_content` IS NULL
  AND `learning_summary` IS NOT NULL;

SET @note_topic_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'note'
    AND COLUMN_NAME = 'topic'
);

SET @note_topic_sql := IF(
  @note_topic_exists = 0,
  'ALTER TABLE `note` ADD COLUMN `topic` VARCHAR(64) DEFAULT NULL AFTER `content_md`',
  'SELECT 1'
);
PREPARE note_topic_stmt FROM @note_topic_sql;
EXECUTE note_topic_stmt;
DEALLOCATE PREPARE note_topic_stmt;

SET @note_tags_json_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'note'
    AND COLUMN_NAME = 'tags_json'
);

SET @note_tags_json_sql := IF(
  @note_tags_json_exists = 0,
  'ALTER TABLE `note` ADD COLUMN `tags_json` JSON DEFAULT NULL AFTER `topic`',
  'SELECT 1'
);
PREPARE note_tags_json_stmt FROM @note_tags_json_sql;
EXECUTE note_tags_json_stmt;
DEALLOCATE PREPARE note_tags_json_stmt;

SET @note_is_public_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'note'
    AND COLUMN_NAME = 'is_public'
);

SET @note_is_public_sql := IF(
  @note_is_public_exists = 0,
  'ALTER TABLE `note` ADD COLUMN `is_public` TINYINT NOT NULL DEFAULT 1 AFTER `tags_json`',
  'SELECT 1'
);
PREPARE note_is_public_stmt FROM @note_is_public_sql;
EXECUTE note_is_public_stmt;
DEALLOCATE PREPARE note_is_public_stmt;

UPDATE `note`
SET `topic` = `note_type`
WHERE `topic` IS NULL;

UPDATE `note`
SET `is_public` = IF(`status` = 'PUBLISHED', 1, 0)
WHERE `status` IS NOT NULL;

SET @timeline_tags_json_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'timeline_event'
    AND COLUMN_NAME = 'tags_json'
);

SET @timeline_tags_json_sql := IF(
  @timeline_tags_json_exists = 0,
  'ALTER TABLE `timeline_event` ADD COLUMN `tags_json` JSON DEFAULT NULL AFTER `content_md`',
  'SELECT 1'
);
PREPARE timeline_tags_json_stmt FROM @timeline_tags_json_sql;
EXECUTE timeline_tags_json_stmt;
DEALLOCATE PREPARE timeline_tags_json_stmt;

SET @timeline_visible_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'timeline_event'
    AND COLUMN_NAME = 'visible'
);

SET @timeline_visible_sql := IF(
  @timeline_visible_exists = 0,
  'ALTER TABLE `timeline_event` ADD COLUMN `visible` TINYINT NOT NULL DEFAULT 1 AFTER `status`',
  'SELECT 1'
);
PREPARE timeline_visible_stmt FROM @timeline_visible_sql;
EXECUTE timeline_visible_stmt;
DEALLOCATE PREPARE timeline_visible_stmt;

UPDATE `timeline_event`
SET `visible` = IF(`status` = 'PUBLISHED', 1, 0)
WHERE `status` IS NOT NULL;

SET @site_group_name_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'site_setting'
    AND COLUMN_NAME = 'group_name'
);

SET @site_group_name_sql := IF(
  @site_group_name_exists = 0,
  'ALTER TABLE `site_setting` ADD COLUMN `group_name` VARCHAR(64) NOT NULL DEFAULT ''general'' AFTER `config_type`',
  'SELECT 1'
);
PREPARE site_group_name_stmt FROM @site_group_name_sql;
EXECUTE site_group_name_stmt;
DEALLOCATE PREPARE site_group_name_stmt;

SET @site_description_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'site_setting'
    AND COLUMN_NAME = 'description'
);

SET @site_description_sql := IF(
  @site_description_exists = 0,
  'ALTER TABLE `site_setting` ADD COLUMN `description` VARCHAR(512) DEFAULT NULL AFTER `config_value`',
  'SELECT 1'
);
PREPARE site_description_stmt FROM @site_description_sql;
EXECUTE site_description_stmt;
DEALLOCATE PREPARE site_description_stmt;

UPDATE `site_setting`
SET `group_name` = CASE
  WHEN `config_key` LIKE 'home.%' THEN 'home'
  WHEN `config_key` LIKE 'about.%' THEN 'about'
  WHEN `config_key` LIKE 'social.%' THEN 'social'
  WHEN `config_key` LIKE 'learning.%' THEN 'home'
  ELSE `group_name`
END
WHERE `group_name` = 'general';

INSERT IGNORE INTO `site_setting` (`config_key`, `config_name`, `config_type`, `group_name`, `config_value`, `description`, `updated_by`)
VALUES
  ('site.hero.title', '首页 Hero 标题', 'TEXT', 'home', '你好，我是 Yu', '首页首屏主标题', 1),
  ('site.hero.subtitle', '首页 Hero 副标题', 'TEXT', 'home', '计算机科学与技术本科生', '首页首屏副标题', 1),
  ('site.hero.description', '首页 Hero 描述', 'TEXT', 'home', '正在构建 Java 后端、Linux、数据库、Vue3 与 AI 应用能力。', '首页首屏描述', 1),
  ('site.hero.status_text', '首页状态文案', 'TEXT', 'home', 'Online & Learning', '首页在线状态文案', 1),
  ('site.currently_learning', '当前学习内容', 'JSON', 'home', '["Spring Boot 3", "MyBatis-Plus", "MySQL 8", "Redis", "Vue3", "Docker"]', '首页当前学习内容', 1),
  ('site.about.profile', '关于我资料', 'JSON', 'about', '{"nickname":"Yu","role":"计算机科学与技术本科生","avatar":"","description":"我正在把个人博客系统打造成一个长期运营的数字花园，用来记录课程设计、实习准备和工程实践。","location":"","email":"","githubUrl":"https://github.com/your-name","careerDirection":["Java 后端开发","Linux 运维","AI 应用开发"]}', '关于我个人资料 JSON', 1),
  ('site.about.skills', '技能清单', 'JSON', 'about', '["Java","Spring Boot","MySQL","Redis","Linux","Vue3","Docker","AI"]', '关于我技能清单', 1),
  ('site.about.education', '教育经历', 'JSON', 'about', '["计算机科学与技术本科在读","持续准备课程设计与实习项目"]', '关于我教育经历', 1),
  ('site.about.philosophy', '学习理念', 'TEXT', 'about', '把每一次课程设计、问题排查和项目实践沉淀为可复用的工程经验。', '关于我学习理念', 1),
  ('site.social.github', 'GitHub 链接', 'URL', 'social', 'https://github.com/your-name', '公开 GitHub 链接', 1);
