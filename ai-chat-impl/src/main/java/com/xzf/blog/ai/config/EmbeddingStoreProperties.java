package com.xzf.blog.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "langchain4j.embedding.milvus")
@Component
public class EmbeddingStoreProperties {
    private String host;
    private Integer port;
    private String collectionName;
    private String username;
    private String password;
}
