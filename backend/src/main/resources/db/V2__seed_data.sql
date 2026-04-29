INSERT IGNORE INTO `sys_user` (
  `id`, `username`, `email`, `password_hash`, `nickname`, `avatar_url`, `role_code`, `status`, `created_at`, `updated_at`, `deleted`
) VALUES (
  1,
  'yu_admin',
  'yu_admin@example.com',
  '{bcrypt}$2a$10$AA4g8dZHPkgE9UKMSBfAPem0uTctQbclwdgfprYl8RWHyYCK/STua',
  'Yu',
  NULL,
  'ADMIN',
  'ENABLED',
  NOW(),
  NOW(),
  0
);

INSERT IGNORE INTO `category` (`id`, `biz_type`, `name`, `slug`, `description`, `sort_order`, `status`)
VALUES
  (100, 'ARTICLE', '后端开发', 'backend-development', 'Java、Spring Boot 与服务端工程实践', 10, 'ENABLED'),
  (101, 'ARTICLE', 'Linux 运维', 'linux-ops', 'Linux、Shell、部署与服务器维护记录', 20, 'ENABLED'),
  (102, 'ARTICLE', '数据库', 'database', 'MySQL、Redis、索引与数据建模笔记', 30, 'ENABLED'),
  (103, 'ARTICLE', 'AI 应用', 'ai-applications', 'AI 助手、提示词和应用开发实验', 40, 'ENABLED'),
  (104, 'ARTICLE', '前端工程', 'frontend-engineering', 'Vue3、Vite 与前端工程化实践', 50, 'ENABLED'),
  (105, 'NOTE', '学习笔记', 'study-notes', '课程设计、实习准备与日常学习记录', 60, 'ENABLED');

INSERT IGNORE INTO `tag` (`id`, `name`, `slug`, `color`, `description`, `status`)
VALUES
  (200, 'Java', 'java', '#f89820', 'Java 后端基础与工程实践', 'ENABLED'),
  (201, 'Spring Boot', 'spring-boot', '#6db33f', 'Spring Boot 应用开发', 'ENABLED'),
  (202, 'MySQL', 'mysql', '#38debb', 'MySQL 建模、索引与查询优化', 'ENABLED'),
  (203, 'Redis', 'redis', '#ff6b6b', 'Redis 缓存、计数和限流', 'ENABLED'),
  (204, 'Linux', 'linux', '#dde4e0', 'Linux 运维和部署', 'ENABLED'),
  (205, 'Vue3', 'vue3', '#42b883', 'Vue3 前端开发', 'ENABLED'),
  (206, 'AI', 'ai', '#d7baff', 'AI 助手与智能应用', 'ENABLED'),
  (207, 'Docker', 'docker', '#2496ed', '容器化与部署', 'ENABLED');

INSERT IGNORE INTO `site_setting` (`id`, `config_key`, `config_name`, `config_type`, `config_value`, `updated_by`)
VALUES
  (300, 'home.title', '首页标题', 'TEXT', '你好，我是 Yu。', 1),
  (301, 'home.subtitle', '首页副标题', 'TEXT', '计算机科学与技术本科生，正在构建 Java 后端、Linux、数据库与 AI 应用能力。', 1),
  (302, 'about.intro', '关于我简介', 'MARKDOWN', '我正在把个人博客系统打造成一个长期运营的数字花园，用来记录课程设计、实习准备和工程实践。', 1),
  (303, 'social.github', 'GitHub 链接占位', 'URL', 'https://github.com/your-name', 1),
  (304, 'learning.current', '当前学习内容', 'JSON', '["Spring Boot 3", "MyBatis-Plus", "MySQL 8", "Redis", "Vue3", "Docker"]', 1);

INSERT IGNORE INTO `article` (
  `id`, `author_user_id`, `category_id`, `title`, `slug`, `summary`, `cover_image_url`,
  `content_md`, `content_html`, `status`, `is_top`, `allow_comment`, `view_count`, `like_count`, `comment_count`, `published_at`
) VALUES
  (
    1000, 1, 100, '个人博客系统后端骨架搭建记录', 'blog-backend-skeleton',
    '记录 Spring Boot 3、统一返回结构、目录分层和健康检查接口的初始化过程。',
    NULL,
    '# 个人博客系统后端骨架搭建记录\n\n本篇记录从静态 UI 走向 CMS 后端的第一步，包括工程目录、统一响应结构和健康检查。',
    NULL, 'PUBLISHED', 1, 1, 128, 12, 0, '2026-04-28 10:00:00'
  ),
  (
    1001, 1, 102, 'MySQL 与 Redis 在博客系统中的职责边界', 'mysql-redis-blog-boundary',
    '整理 MySQL 存储核心内容、Redis 承担缓存和计数缓冲的设计边界。',
    NULL,
    '# MySQL 与 Redis 在博客系统中的职责边界\n\nMySQL 负责文章、项目、笔记和审核数据，Redis 后续用于首页缓存、浏览量和点赞计数。',
    NULL, 'PUBLISHED', 0, 1, 86, 7, 0, '2026-04-28 11:00:00'
  ),
  (
    1002, 1, 104, 'Vue3 前端迁移计划草稿', 'vue3-stitch-migration-plan',
    '将 Stitch 静态页面拆分为 Vue 路由、布局和组件的迁移草稿。',
    NULL,
    '# Vue3 前端迁移计划草稿\n\n先保留视觉风格，逐步把首页、文章列表、项目页和后台页拆成组件。',
    NULL, 'DRAFT', 0, 0, 0, 0, 0, NULL
  );

INSERT IGNORE INTO `article_tag` (`article_id`, `tag_id`)
VALUES
  (1000, 200),
  (1000, 201),
  (1001, 202),
  (1001, 203),
  (1002, 205);

INSERT IGNORE INTO `project` (
  `id`, `name`, `slug`, `description`, `cover_image_url`, `tech_stack_json`, `repo_url`, `demo_url`,
  `status`, `learning_summary`, `is_featured`, `sort_order`
) VALUES
  (
    1100, 'YU.LOG 个人博客系统', 'yu-log-blog-system',
    '从 Stitch 静态 UI 演进为可运营 CMS 的个人博客系统。',
    NULL,
    JSON_ARRAY('Java 21', 'Spring Boot 3', 'MySQL 8', 'Redis', 'Vue3', 'Tailwind CSS'),
    'https://github.com/your-name/yu-log',
    NULL,
    'PUBLISHED',
    '用于串联后端开发、数据库建模、前端工程和部署上线能力。',
    1,
    10
  ),
  (
    1101, 'AI 学习助手实验', 'ai-learning-assistant-lab',
    '围绕课程复习、实习准备和博客内容整理的 AI 助手原型。',
    NULL,
    JSON_ARRAY('AI', 'Prompt Engineering', 'Vue3', 'Spring Boot'),
    NULL,
    NULL,
    'PUBLISHED',
    '探索把 AI 助手接入个人知识库和学习流程。',
    0,
    20
  );

INSERT IGNORE INTO `note` (
  `id`, `author_user_id`, `category_id`, `title`, `slug`, `summary`, `content_md`,
  `note_type`, `status`, `is_pinned`, `sort_order`
) VALUES
  (
    1200, 1, 105, 'Spring Boot 配置分环境记录', 'spring-boot-profile-notes',
    '记录 dev/prod 配置拆分、敏感信息外置和启动参数。',
    '# Spring Boot 配置分环境记录\n\n开发环境使用本地 Docker MySQL，生产环境通过环境变量注入敏感配置。',
    'NOTE', 'PUBLISHED', 1, 10
  ),
  (
    1201, 1, 105, 'Linux 部署命令清单', 'linux-deploy-command-list',
    '整理服务启动、日志查看、端口检查和 Docker 常用命令。',
    '# Linux 部署命令清单\n\n包括 systemctl、journalctl、docker ps、ss -lntp 等常用命令。',
    'NOTE', 'PUBLISHED', 0, 20
  ),
  (
    1202, 1, 105, '实习准备复盘模板', 'internship-review-template',
    '用于记录每日学习、项目进度和面试题复盘的笔记模板。',
    '# 实习准备复盘模板\n\n今日学习、项目推进、问题记录、明日计划。',
    'TIL', 'PUBLISHED', 0, 30
  );

INSERT IGNORE INTO `timeline_event` (
  `id`, `title`, `event_date`, `event_type`, `summary`, `content_md`,
  `related_article_id`, `related_project_id`, `status`, `sort_order`
) VALUES
  (
    1300, '确定个人博客 CMS 化目标', '2026-04-28', 'PROJECT',
    '明确从 Stitch 静态 UI 走向可运营博客系统的阶段计划。',
    '完成需求分析、技术栈确认和阶段划分。',
    1000, 1100, 'PUBLISHED', 10
  ),
  (
    1301, '完成工程骨架初始化', '2026-04-28', 'MILESTONE',
    '前端 Vue3 骨架和后端 Spring Boot 骨架可以启动。',
    '完成路由占位、Tailwind 主题、Result/PageResult 和健康检查。',
    1000, 1100, 'PUBLISHED', 20
  ),
  (
    1302, '准备数据库与基础实体', '2026-04-28', 'DATABASE',
    '建立 MySQL 表结构、种子数据、Entity、Mapper 和最小查询验证。',
    '为后续登录认证、文章管理和评论留言打基础。',
    1001, 1100, 'PUBLISHED', 30
  );

INSERT IGNORE INTO `comment` (`id`, `article_id`, `nickname`, `email`, `content`, `status`, `ip_hash`, `user_agent`)
VALUES
  (1400, 1000, '访客示例', 'visitor@example.com', '期待看到这个博客系统逐步完善。', 'PENDING', 'seed-ip-hash', 'seed-agent');

INSERT IGNORE INTO `message` (`id`, `nickname`, `email`, `content`, `reply_content`, `reply_user_id`, `status`, `ip_hash`, `user_agent`)
VALUES
  (1500, '课程设计访客', 'guest@example.com', '希望后续能看到课程设计和实习准备的经验记录。', NULL, NULL, 'PENDING', 'seed-ip-hash', 'seed-agent');
