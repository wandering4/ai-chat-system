package com.xzf.blog.ai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiAgentConfig {

    @Autowired
    private DeepSeekProperties deepSeekProperties;

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(deepSeekProperties.getApiKey())
                .modelName(deepSeekProperties.getModelName())
                .baseUrl(deepSeekProperties.getBaseUrl())
                .temperature(deepSeekProperties.getTemperature())
                .maxTokens(deepSeekProperties.getMaxTokens())
                .logRequests(deepSeekProperties.getLogRequests())
                .logResponses(deepSeekProperties.getLogResponses())
                .build();
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(deepSeekProperties.getApiKey())
                .modelName(deepSeekProperties.getModelName())
                .baseUrl(deepSeekProperties.getBaseUrl())
                .temperature(deepSeekProperties.getTemperature())
                .maxTokens(deepSeekProperties.getMaxTokens())
                .logRequests(deepSeekProperties.getLogRequests())
                .logResponses(deepSeekProperties.getLogResponses())
                .build();
    }

}
