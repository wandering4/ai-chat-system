package com.xzf.blog.ai.consumer;

import com.google.common.util.concurrent.RateLimiter;
import com.xzf.blog.ai.api.vo.req.ArticleReq;
import com.xzf.blog.ai.service.RAGService;
import com.xzf.blog.article.constants.MQConstants;
import com.xzf.blog.article.dto.mq.ArticleMessage;
import com.xzf.blog.framework.commons.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 文章发布更新消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "ai_chat_group_rag_" + MQConstants.TOPIC_DELETE_ARTICLE, // Group 组
        topic = MQConstants.TOPIC_DELETE_ARTICLE // 主题 Topic
)
public class ArticleDeleteRAGConsumer implements RocketMQListener<Long> {

    @Autowired
    private RAGService ragService;

    @Override
    public void onMessage(Long articleId) {
        log.info("## ArticleDeleteRAGConsumer消费到了 MQ 【智能服务：RAG切分文章内容】, {}...", articleId);
        // 删除切片
        ragService.deleteByArticleId(articleId);
    }

}
