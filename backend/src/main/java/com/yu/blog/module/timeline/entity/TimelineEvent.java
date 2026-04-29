package com.yu.blog.module.timeline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("timeline_event")
public class TimelineEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private LocalDate eventDate;
    private String eventType;
    private String summary;
    private String contentMd;
    private Long relatedArticleId;
    private Long relatedProjectId;
    private String status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
