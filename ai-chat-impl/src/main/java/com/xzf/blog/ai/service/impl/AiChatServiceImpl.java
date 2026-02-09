package com.xzf.blog.ai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xzf.blog.ai.api.vo.req.ChatHistoryReq;
import com.xzf.blog.ai.api.vo.req.ChatReq;
import com.xzf.blog.ai.api.vo.req.RagasReq;
import com.xzf.blog.ai.api.vo.resp.ChatHistoryRespVO;
import com.xzf.blog.ai.api.vo.resp.RagasRespVO;
import com.xzf.blog.ai.commons.constant.RedisConstants;
import com.xzf.blog.ai.commons.enums.ChatType;
import com.xzf.blog.ai.config.AbstractModelProperties;
import com.xzf.blog.ai.config.ChatModelProperties;
import com.xzf.blog.ai.manager.ChatHistoryManager;
import com.xzf.blog.ai.model.ChatHistoryDO;
import com.xzf.blog.ai.service.AiChatService;
import com.xzf.blog.ai.service.RAGService;
import com.xzf.blog.framework.commons.response.PageResponse;
import com.xzf.blog.framework.commons.util.JsonUtils;
import com.xzf.framework.biz.context.holder.LoginUserContextHolder;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.xzf.blog.framework.commons.response.Response;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisCallback;

import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import dev.langchain4j.model.StreamingResponseHandler;


@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    @Autowired
    private AbstractModelProperties abstractModelProperties;

    @Autowired
    private ChatModelProperties chatModelProperties;

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private StreamingChatLanguageModel streamingChatLanguageModel;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ChatHistoryManager chatHistoryManager;

    @Autowired
    private RAGService ragService;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Value("${system.chat.opening-remark}")
    private String openingRemark;


    public Response<String> generateAbstract(String article) {
        try {
            String systemPrompt = abstractModelProperties.getSystemPrompt();

            // 使用DeepSeek模型生成摘要
            ChatResponse chatResponse = chatLanguageModel.chat(ChatRequest.builder().messages(List.of(new SystemMessage(systemPrompt), new UserMessage(article))).build());
            String content = chatResponse.aiMessage().text();

            log.info("文章内容长度:{}, 生成摘要:{}", article.length(), content);
            return Response.success(content);
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            return Response.fail("摘要生成失败，请稍后重试");
        }
    }

    @Override
    public Flux<String> streamingChat(ChatReq req) {
        return Flux.create(sink -> {
            try {
                String conversationKey = req.getConversationKey();
                String message = req.getMessage();
                Long userId = LoginUserContextHolder.getUserId();

                int maxRound = chatModelProperties.getMaxRound();
                int historyRound = chatModelProperties.getHistoryRound();
                long expireSeconds = chatModelProperties.getExpireSeconds();
                String chatSystemPrompt = chatModelProperties.getChatSystemPrompt();

                String redisKey = RedisConstants.getChatConversationKey(userId, conversationKey);
                Long round = chatHistoryManager.getRound(userId, conversationKey);
                if (round > maxRound) {
                    sink.error(new RuntimeException("对话轮次过多，请重新开始对话"));
                    return;
                }

                // 查询历史对话记录
                List<String> chatCache = stringRedisTemplate.opsForList().range(redisKey, 0, historyRound);
                List<ChatHistoryDO> chatHistoryDOList = null;
                if (chatCache != null && !chatCache.isEmpty()) {
                    // 从缓存中获取历史对话记录
                    chatHistoryDOList = chatCache.stream().map(str -> JsonUtils.parseObject(str, ChatHistoryDO.class)).collect(Collectors.toList());
                } else {
                    // 查询db中的历史对话记录
                    chatHistoryDOList = chatHistoryManager.getHistory(userId, conversationKey, historyRound);
                }

                // RAG检索获取片段
                List<String> contextList = ragService.search(req.getArticleId(), message).stream().map(TextSegment::text).toList();
                log.info("query:{},检索到的片段:{}", req.getMessage(), JsonUtils.toJsonString(contextList));
                String segments = String.join(",", contextList);
                chatSystemPrompt += segments;

                // 构建消息列表
                List<ChatMessage> messages = chatHistoryDOList.stream().map(chatHistoryDO -> new UserMessage(JsonUtils.toJsonString(chatHistoryDO))).collect(Collectors.toList());
                messages.add(new UserMessage("用户本次提问问题:" + req.getMessage()));
                messages.add(new SystemMessage(chatSystemPrompt));

                final List<ChatHistoryDO> finalChatHistoryDOList = chatHistoryDOList;
                // 使用StreamingResponseHandler处理流式响应
                streamingChatLanguageModel.generate(messages, new StreamingResponseHandler() {
                    @Override
                    public void onNext(String token) {
                        sink.next(token);
                    }

                    @Override
                    public void onComplete(dev.langchain4j.model.output.Response response) {
                        log.info("用户提问:{}, 模型回答:{}，使用token数:{}", req.getMessage(), response.content(), response.tokenUsage().totalTokenCount());
                        // 保存对话记录到数据库
                        ChatHistoryDO question = ChatHistoryDO.builder().accountId(userId).conversationKey(conversationKey).type(ChatType.USER_MESSAGE).content(JsonUtils.toJsonString(req)).build();
                        ChatHistoryDO answer = ChatHistoryDO.builder().accountId(userId).conversationKey(conversationKey).type(ChatType.AI_MESSAGE).content(response.content().toString()).build();
                        chatHistoryManager.save(question);
                        chatHistoryManager.save(answer);

                        // 异步进行缓存加载
                        finalChatHistoryDOList.add(question);
                        finalChatHistoryDOList.add(answer);

                        final List<String> cacheList = finalChatHistoryDOList.stream().map(JsonUtils::toJsonString).collect(Collectors.toList());
                        loadCacheAsync(redisKey, cacheList, expireSeconds);
                        sink.complete();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.error("流式对话发生错误", throwable);
                        sink.error(throwable);
                    }
                });

            } catch (Exception e) {
                log.error("流式对话初始化失败", e);
                sink.error(e);
            }
        });
    }

    @Override
    public Response<RagasRespVO> ragasQuery(RagasReq req) {
        boolean hyde = req.isHyde();
        String query = req.getQuery();
        String chatSystemPrompt = chatModelProperties.getChatSystemPrompt();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new UserMessage("用户本次提问问题:" + query));
        // 如果使用hyde技术，拿ai对话结果检索
        if (hyde) {
            AiMessage aiMessage = chatLanguageModel.generate(messages).content();
            query = aiMessage.text();
            log.info("问题:{} ,Hyde技术生成文本:{}", req.getQuery(), query);
        }

        // RAG检索获取片段
        List<String> contexts = ragService.search(null, query).stream().map(TextSegment::text).toList();
        log.info("query:{},检索到的片段:{}", req.getQuery(), JsonUtils.toJsonString(contexts));
        String segments = String.join(",", contexts);
        chatSystemPrompt += segments;
        // 构建消息列表
        messages.add(new SystemMessage(chatSystemPrompt));
        AiMessage answerMessage = chatLanguageModel.generate(messages).content();

        RagasRespVO vo = RagasRespVO.builder()
                .answer(answerMessage.text())
                .contexts(contexts)
                .build();
        log.info("模型实际回答:{}", JsonUtils.toJsonString(vo));
        return Response.success(vo);
    }

    @Override
    public PageResponse<ChatHistoryRespVO> getHistoryChat(ChatHistoryReq req) {
        Long current = req.getCurrent();
        Long size = req.getSize();
        Long userId = LoginUserContextHolder.getUserId();
        String conversationKey = req.getConversationKey();

        Page<ChatHistoryDO> chatHistoryDOPage = chatHistoryManager.selectPageList(current, size, conversationKey, userId);
        List<ChatHistoryDO> records = chatHistoryDOPage.getRecords();

        List<ChatHistoryRespVO> vos = null;
        if (!CollectionUtils.isEmpty(records)) {
            vos = records.stream().map(chatHistoryDO -> {
                ChatHistoryRespVO vo = new ChatHistoryRespVO();
                BeanUtils.copyProperties(chatHistoryDO, vo);
                return vo;
            }).toList();
        } else {
            vos = Collections.singletonList(ChatHistoryRespVO.builder().type(ChatType.AI_MESSAGE).createTime(LocalDateTime.now()).content(openingRemark).build());
        }

        return PageResponse.success(chatHistoryDOPage, vos);
    }

    private void loadCacheAsync(String redisKey, List<String> cacheList, long expireSeconds) {
        threadPoolTaskExecutor.execute(() -> {
            // 先删除全部，再加载list到缓存中
            stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                byte[] keyBytes = redisKey.getBytes(StandardCharsets.UTF_8);
                connection.del(keyBytes);
                for (String item : cacheList) {
                    connection.rPush(keyBytes, item.getBytes(StandardCharsets.UTF_8));
                }
                connection.expire(keyBytes, expireSeconds);
                return null;
            });
        });
    }


}
