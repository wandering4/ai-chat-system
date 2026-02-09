package com.xzf.blog.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "langchain4j.embedding.model")
@Component
public class EmbeddingModelProperties {
    private String baseUrl;
    private String apiKey;
    private String modelName;

}
