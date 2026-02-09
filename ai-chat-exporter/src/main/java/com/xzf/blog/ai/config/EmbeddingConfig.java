package com.xzf.blog.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class EmbeddingConfig {

    @Autowired
    private EmbeddingModelProperties embeddingModelProperties;

    @Autowired
    private EmbeddingStoreProperties embeddingStoreProperties;

    @Bean
    public EmbeddingModel embeddingModel() {
        return ZhipuAiEmbeddingModel.builder()
                .baseUrl(embeddingModelProperties.getBaseUrl())
                .apiKey(embeddingModelProperties.getApiKey())
                .model(embeddingModelProperties.getModelName())
                .connectTimeout(Duration.ofSeconds(20))
                .callTimeout(Duration.ofSeconds(20))
                .writeTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(20))
                .maxRetries(3)
                .build();
    }


    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) {
        return MilvusEmbeddingStore.builder()
                .host(embeddingStoreProperties.getHost())                         // Host for Milvus instance
                .port(embeddingStoreProperties.getPort())                               // Port for Milvus instance
                .collectionName(embeddingStoreProperties.getCollectionName())      // Name of the collection
                .dimension(embeddingModel.dimension())                           // 智谱 embedding-3 模型输出维度为 2048
                .indexType(IndexType.FLAT)                 // Index type
                .metricType(MetricType.COSINE)             // Metric type
                .username(embeddingStoreProperties.getUsername())                      // Username for Milvus
                .password(embeddingStoreProperties.getPassword())                      // Password for Milvus
                .consistencyLevel(ConsistencyLevelEnum.EVENTUALLY)  // Consistency level
                .autoFlushOnInsert(true)                   // Auto flush after insert
                .build();
    }

}
