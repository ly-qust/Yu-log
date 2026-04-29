package com.yu.blog.module.timeline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.blog.module.timeline.entity.TimelineEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TimelineEventMapper extends BaseMapper<TimelineEvent> {
}
