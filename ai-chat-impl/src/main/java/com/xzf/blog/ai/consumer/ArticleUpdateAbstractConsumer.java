package com.xzf.blog.ai.consumer;

import com.google.common.util.concurrent.RateLimiter;
import com.xzf.blog.ai.service.AiChatService;
import com.xzf.blog.article.api.ArticleFeignApi;
import com.xzf.blog.article.constants.MQConstants;
import com.xzf.blog.article.dto.mq.ArticleMessage;
import com.xzf.blog.article.dto.request.article.UpdateArticleSummaryRequest;
import com.xzf.blog.framework.commons.response.Response;
import com.xzf.blog.framework.commons.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "ai_chat_group_abstract_" + MQConstants.TOPIC_UPDATE_ARTICLE, // Group 组
        topic = MQConstants.TOPIC_UPDATE_ARTICLE // 主题 Topic
)
public class ArticleUpdateAbstractConsumer implements RocketMQListener<String> {

    @Resource
    private AiChatService aiChatService;

    @Resource
    private ArticleFeignApi articleRpcService;

    private RateLimiter rateLimiter = RateLimiter.create(10);

    @Override
    public void onMessage(String body) {
        rateLimiter.tryAcquire();

        log.info("## 消费到了 MQ 【智能服务：生成文章智能摘要】, {}...", body);

        try {
            ArticleMessage articleVO = JsonUtils.parseObject(body, ArticleMessage.class);
            String article = String.format("文章标题:%s,文章内容:%s", articleVO.getTitle(), articleVO.getContent());

            Response<String> response = aiChatService.generateAbstract(article);
            if (!response.isSuccess()) {
                log.error("## 生成文章智能摘要失败: {}", response.getMessage());
                return;
            }
            String summary = response.getData();
            log.info("## 生成文章:{},智能摘要: {}", article, summary);

            Response<?> resp = articleRpcService.updateArticleSummary(UpdateArticleSummaryRequest.builder()
                    .id(articleVO.getArticleId())
                    .summary(summary)
                    .build());
            if (!resp.isSuccess()) {
                log.error("智能摘要更新失败:{}", JsonUtils.toJsonString(resp));
            } else {
                log.info("智能摘要更新成功: {}", JsonUtils.toJsonString(resp));
            }
        } catch (Exception e) {
            log.error("## 解析 JSON 字符串异常", e);
        }
    }

}
