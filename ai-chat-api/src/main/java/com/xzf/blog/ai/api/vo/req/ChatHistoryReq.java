package com.xzf.blog.ai.api.vo.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryReq {

    private Long current = 1L;
    private Long size = 10L;

    @NotBlank(message = "对话唯一键不能为空")
    private String conversationKey;

}
