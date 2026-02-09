package com.xzf.blog.ai.api.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResp {

    private String aiMessage;
    private Integer totalTokens;

}
