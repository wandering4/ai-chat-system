package com.xzf.blog.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xzf.blog.ai.model.ChatHistoryDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistoryDO> {

}
