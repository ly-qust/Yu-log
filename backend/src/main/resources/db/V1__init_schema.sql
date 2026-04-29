CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `email` VARCHAR(128) DEFAULT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(64) NOT NULL,
  `avatar_url` VARCHAR(512) DEFAULT NULL,
  `role_code` VARCHAR(32) NOT NULL DEFAULT 'USER',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  `last_login_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  UNIQUE KEY `uk_sys_user_email` (`email`),
  KEY `idx_sys_user_role_status` (`role_code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `site_setting` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(128) NOT NULL,
  `config_name` VARCHAR(128) NOT NULL,
  `config_type` VARCHAR(32) NOT NULL DEFAULT 'TEXT',
  `config_value` TEXT NOT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_site_setting_config_key` (`config_key`),
  KEY `idx_site_setting_updated_by` (`updated_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `biz_type` VARCHAR(32) NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `slug` VARCHAR(120) NOT NULL,
  `description` VARCHAR(512) DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_biz_type_slug` (`biz_type`, `slug`),
  KEY `idx_category_biz_sort_status` (`biz_type`, `sort_order`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `slug` VARCHAR(120) NOT NULL,
  `color` VARCHAR(32) DEFAULT NULL,
  `description` VARCHAR(512) DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_slug` (`slug`),
  UNIQUE KEY `uk_tag_name` (`name`),
  KEY `idx_tag_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `author_user_id` BIGINT NOT NULL,
  `category_id` BIGINT DEFAULT NULL,
  `title` VARCHAR(180) NOT NULL,
  `slug` VARCHAR(180) NOT NULL,
  `summary` VARCHAR(512) DEFAULT NULL,
  `cover_image_url` VARCHAR(512) DEFAULT NULL,
  `content_md` MEDIUMTEXT NOT NULL,
  `content_html` MEDIUMTEXT DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  `is_top` TINYINT NOT NULL DEFAULT 0,
  `allow_comment` TINYINT NOT NULL DEFAULT 1,
  `view_count` BIGINT NOT NULL DEFAULT 0,
  `like_count` BIGINT NOT NULL DEFAULT 0,
  `comment_count` BIGINT NOT NULL DEFAULT 0,
  `published_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_slug` (`slug`),
  KEY `idx_article_status_published` (`status`, `published_at`),
  KEY `idx_article_category_status_published` (`category_id`, `status`, `published_at`),
  KEY `idx_article_author_created` (`author_user_id`, `created_at`),
  FULLTEXT KEY `ft_article_search` (`title`, `summary`, `content_md`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `article_tag` (
  `article_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`article_id`, `tag_id`),
  KEY `idx_article_tag_tag_article` (`tag_id`, `article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `project` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(120) NOT NULL,
  `slug` VARCHAR(160) NOT NULL,
  `description` VARCHAR(1024) DEFAULT NULL,
  `cover_image_url` VARCHAR(512) DEFAULT NULL,
  `tech_stack_json` JSON DEFAULT NULL,
  `repo_url` VARCHAR(512) DEFAULT NULL,
  `demo_url` VARCHAR(512) DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  `learning_summary` TEXT DEFAULT NULL,
  `is_featured` TINYINT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_slug` (`slug`),
  KEY `idx_project_status_featured_sort` (`status`, `is_featured`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `note` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `author_user_id` BIGINT NOT NULL,
  `category_id` BIGINT DEFAULT NULL,
  `title` VARCHAR(180) NOT NULL,
  `slug` VARCHAR(180) NOT NULL,
  `summary` VARCHAR(512) DEFAULT NULL,
  `content_md` MEDIUMTEXT NOT NULL,
  `note_type` VARCHAR(32) NOT NULL DEFAULT 'NOTE',
  `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  `is_pinned` TINYINT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_slug` (`slug`),
  KEY `idx_note_category_status` (`category_id`, `status`),
  KEY `idx_note_type_status` (`note_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `timeline_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(180) NOT NULL,
  `event_date` DATE NOT NULL,
  `event_type` VARCHAR(32) NOT NULL,
  `summary` VARCHAR(512) DEFAULT NULL,
  `content_md` MEDIUMTEXT DEFAULT NULL,
  `related_article_id` BIGINT DEFAULT NULL,
  `related_project_id` BIGINT DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_timeline_event_date_status` (`event_date`, `status`),
  KEY `idx_timeline_related_article` (`related_article_id`),
  KEY `idx_timeline_related_project` (`related_project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `article_id` BIGINT NOT NULL,
  `parent_id` BIGINT DEFAULT NULL,
  `user_id` BIGINT DEFAULT NULL,
  `nickname` VARCHAR(64) NOT NULL,
  `email` VARCHAR(128) DEFAULT NULL,
  `content` TEXT NOT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  `ip_hash` VARCHAR(128) DEFAULT NULL,
  `user_agent` VARCHAR(512) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_comment_article_status_created` (`article_id`, `status`, `created_at`),
  KEY `idx_comment_parent_created` (`parent_id`, `created_at`),
  KEY `idx_comment_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `nickname` VARCHAR(64) NOT NULL,
  `email` VARCHAR(128) DEFAULT NULL,
  `content` TEXT NOT NULL,
  `reply_content` TEXT DEFAULT NULL,
  `reply_user_id` BIGINT DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  `ip_hash` VARCHAR(128) DEFAULT NULL,
  `user_agent` VARCHAR(512) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_message_status_created` (`status`, `created_at`),
  KEY `idx_message_reply_user` (`reply_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `operator_user_id` BIGINT DEFAULT NULL,
  `module` VARCHAR(64) NOT NULL,
  `action` VARCHAR(64) NOT NULL,
  `biz_id` BIGINT DEFAULT NULL,
  `request_method` VARCHAR(16) DEFAULT NULL,
  `request_uri` VARCHAR(512) DEFAULT NULL,
  `request_id` VARCHAR(128) DEFAULT NULL,
  `ip` VARCHAR(64) DEFAULT NULL,
  `user_agent` VARCHAR(512) DEFAULT NULL,
  `request_json` JSON DEFAULT NULL,
  `response_json` JSON DEFAULT NULL,
  `success` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_operation_operator_time` (`operator_user_id`, `created_at`),
  KEY `idx_operation_module_time` (`module`, `created_at`),
  KEY `idx_operation_request_id` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
