package com.xzf.blog.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "system.chat.deepseek")
@Component
public class DeepSeekProperties {

    private String apiKey;
    private String modelName = "deepseek-chat";
    private String baseUrl = "https://api.deepseek.com/v1";
    private Double temperature = 0.7;
    private Integer maxTokens = 1000;
    private Boolean logRequests = true;
    private Boolean logResponses = true;

}