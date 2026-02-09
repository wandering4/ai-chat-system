package com.xzf.blog.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "system.chat")
@Component
public class ChatModelProperties {

    private int maxRound = 10;

    private int historyRound = 4;

    private long expireSeconds = 60 * 60 * 12;

    private String chatSystemPrompt = "以下是相关的知识片段:";

}
