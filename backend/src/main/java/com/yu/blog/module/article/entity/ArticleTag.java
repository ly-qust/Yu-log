package com.yu.blog.module.article.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("article_tag")
public class ArticleTag {
    @TableId
    private Long articleId;
    private Long tagId;
    private LocalDateTime createdAt;
}
