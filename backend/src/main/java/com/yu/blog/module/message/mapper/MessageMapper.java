package com.yu.blog.module.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.blog.module.message.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
