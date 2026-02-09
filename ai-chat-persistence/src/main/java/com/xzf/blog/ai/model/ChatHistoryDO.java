package com.xzf.blog.ai.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xzf.blog.ai.commons.enums.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_history")
public class ChatHistoryDO {

    @TableId
    private Long id;

    @TableField("conversation_key")
    private String conversationKey;

    @TableField("account_id")
    private Long accountId;

    @TableField("type")
    private ChatType type;

    @TableField("content")
    private String content;

    @TableField("create_time")
    private LocalDateTime createTime;

}
