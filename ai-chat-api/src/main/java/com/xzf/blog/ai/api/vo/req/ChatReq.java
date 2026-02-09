package com.xzf.blog.ai.api.vo.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatReq {

    private Long articleId;

    @NotBlank(message = "对话唯一键不能为空")
    private String conversationKey;

    @NotBlank(message = "对话消息不能为空")
    private String message;

    private boolean hyde = false;
}
