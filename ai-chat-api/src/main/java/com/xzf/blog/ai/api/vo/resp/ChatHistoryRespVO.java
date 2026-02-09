package com.xzf.blog.ai.api.vo.resp;

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
public class ChatHistoryRespVO {

    private ChatType type;

    private String content;

    private LocalDateTime createTime;

}
