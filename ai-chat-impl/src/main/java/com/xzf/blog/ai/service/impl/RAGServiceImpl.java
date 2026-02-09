package com.xzf.blog.ai.service.impl;

import com.xzf.blog.ai.api.vo.req.ArticleReq;
import com.xzf.blog.ai.config.EmbeddingStoreProperties;
import com.xzf.blog.ai.service.RAGService;
import com.xzf.blog.ai.strategy.SplitStrategy;
import com.xzf.blog.ai.util.SlidingWindowUtil;
import com.xzf.blog.framework.commons.util.JsonUtils;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsNotEqualTo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RAGServiceImpl implements RAGService {

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private EmbeddingStoreProperties embeddingStoreProperties;

    private static final String ARTICLE_ID = "articleId";

    @Value("${article.split.strategy:slidingWindow}")
    private String splitStrategy;

    @Resource
    private List<SplitStrategy> splitStrategies;


    @Override
    public boolean uploadFile(List<MultipartFile> files) {
        log.info("上传知识库开始");

        for (MultipartFile file : files) {
            try (InputStream inputStream = file.getInputStream()) {
                // 使用 Tika 解析文档
                Document document = new ApacheTikaDocumentParser().parse(inputStream);
                DocumentSplitter splitter = DocumentSplitters.recursive(300, 10);
                List<TextSegment> segments = splitter.split(document);
                /*使用向量模型处理返回向量数据*/
                List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

                /*存入向量数据库（使用注入的 Milvus embeddingStore）*/
                embeddingStore.addAll(embeddings, segments);
                log.info("上传知识库完成，共存入 {} 个片段", segments.size());

            } catch (IOException e) {
                log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean uploadArticle(ArticleReq req) {
        Long articleId = req.getArticleId();
        // 滑动窗口切分文章为文本片段
        String[] paragraphs = getStrategy().split(req.getContent());
        List<TextSegment> segments = new ArrayList<>();
        for (int i = 0; i < paragraphs.length; i++) {
            String text = String.format("文章标题:%s,文章片段:%s", req.getTitle(), paragraphs[i]);
            TextSegment segment = TextSegment.from(text);
            segment.metadata().put("articleId", articleId);
            segment.metadata().put("segmentIndex", i);
            segment.metadata().put("title", req.getTitle());
            segments.add(segment);
        }
        /*使用向量模型处理返回向量数据*/
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
        return true;
    }

    @Override
    public List<TextSegment> search(Long articleId, String query) {
        // 生成查询向量
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        if (articleId == null) {
            return embeddingStore.search(EmbeddingSearchRequest.builder()
                            .queryEmbedding(queryEmbedding)
                            .maxResults(3) // 默认返回前3条
                            .minScore(0.6) // 最小相似度分数
                            .build()).matches().stream()
                    .map(EmbeddingMatch::embedded)
                    .collect(Collectors.toList());
        }

        // 执行搜索
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(10) // 默认返回前3条
                .minScore(0.6) // 最小相似度分数
                .build());

        List<TextSegment> textSegments = result.matches()
                .stream()
                .map(EmbeddingMatch::embedded)
                .toList();
        return textSegments.stream().filter(segment -> {
                    String articleStr = segment.metadata().get(ARTICLE_ID);
                    if(articleStr==null) return false;
                    try {
                        // 先转为 Double 再转为 Long，处理 "21.0" 这种情况
                        Double doubleValue = Double.valueOf(articleStr);
                        Long id = doubleValue.longValue();
                        return id.equals(articleId);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }).limit(3) // 返回前3个匹配的
                .toList();
    }


    @Override
    public boolean deleteByArticleId(Long articleId) {
        try {
            // 执行删除
            embeddingStore.removeAll(new IsEqualTo(ARTICLE_ID,articleId));
            log.info("删除文章片段成功, articleId: {}", articleId);
            return true;
        } catch (Exception e) {
            log.error("删除文章片段失败, articleId: {}", articleId, e);
            return false;
        }
    }

    private SplitStrategy getStrategy(){
        for (SplitStrategy strategy : splitStrategies) {
            if(strategy.getName().equals(splitStrategy)) return strategy;
        }
        return null;
    }

}