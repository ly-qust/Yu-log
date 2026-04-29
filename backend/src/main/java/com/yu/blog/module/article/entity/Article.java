package com.yu.blog.module.article.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long authorUserId;
    private Long categoryId;
    private String title;
    private String slug;
    private String summary;
    private String coverImageUrl;
    private String contentMd;
    private String contentHtml;
    private String status;
    @TableField("is_top")
    private Boolean isTop;
    private Boolean allowComment;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
