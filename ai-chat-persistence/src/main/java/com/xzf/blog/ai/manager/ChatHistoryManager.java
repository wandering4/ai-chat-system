package com.xzf.blog.ai.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzf.blog.ai.mapper.ChatHistoryMapper;
import com.xzf.blog.ai.model.ChatHistoryDO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ChatHistoryManager extends ServiceImpl<ChatHistoryMapper, ChatHistoryDO> {

    public long getRound(Long userId, String conversationKey) {
        LambdaQueryWrapper<ChatHistoryDO> queryWrapper = new LambdaQueryWrapper<ChatHistoryDO>()
                .eq(ChatHistoryDO::getAccountId, userId)
                .eq(ChatHistoryDO::getConversationKey, conversationKey);
        return count(queryWrapper);
    }

    public List<ChatHistoryDO> getHistory(Long userId, String conversationKey, int historyRound) {
        LambdaQueryWrapper<ChatHistoryDO> queryWrapper = new LambdaQueryWrapper<ChatHistoryDO>()
                .eq(ChatHistoryDO::getAccountId, userId)
                .eq(ChatHistoryDO::getConversationKey, conversationKey)
                .orderByDesc(ChatHistoryDO::getId)
                .last("limit " + historyRound);
        return list(queryWrapper);
    }

    public Page<ChatHistoryDO> selectPageList(Long current, Long size, String conversationKey, Long userId) {
        // 分页对象(查询第几页、每页多少数据)
        Page<ChatHistoryDO> page = new Page<>(current, size);
        // 构建查询条件
        LambdaQueryWrapper<ChatHistoryDO> wrapper = Wrappers.<ChatHistoryDO>lambdaQuery()
                .eq(ChatHistoryDO::getAccountId, userId)
                .eq(ChatHistoryDO::getConversationKey, conversationKey)
                .orderByDesc(ChatHistoryDO::getCreateTime); // 按创建时间倒叙
        return page(page, wrapper);
    }
}
