package com.xzf.blog.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "system.abstract")
@Component
public class AbstractModelProperties {

    private String systemPrompt;

    private String model;

}
