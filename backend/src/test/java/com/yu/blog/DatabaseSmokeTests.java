package com.yu.blog;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yu.blog.module.article.entity.Article;
import com.yu.blog.module.article.mapper.ArticleMapper;
import com.yu.blog.module.user.entity.SysUser;
import com.yu.blog.module.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DatabaseSmokeTests {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Test
    void shouldQuerySeededAdminUser() {
        SysUser admin = sysUserMapper.selectOne(
                Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, "yu_admin")
        );

        assertThat(admin).isNotNull();
        assertThat(admin.getRoleCode()).isEqualTo("ADMIN");
        assertThat(admin.getPasswordHash()).startsWith("{bcrypt}");
    }

    @Test
    void shouldQuerySeededArticles() {
        Long articleCount = articleMapper.selectCount(null);
        Article article = articleMapper.selectOne(
                Wrappers.lambdaQuery(Article.class).eq(Article::getSlug, "blog-backend-skeleton")
        );

        assertThat(articleCount).isGreaterThanOrEqualTo(3);
        assertThat(article).isNotNull();
        assertThat(article.getStatus()).isEqualTo("PUBLISHED");
    }
}
