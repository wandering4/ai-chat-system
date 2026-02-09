package com.xzf.blog.ai.controller;//package com.xzf.ai.chat.trigger.http;

import com.xzf.blog.ai.api.IAiChatApiService;
import com.xzf.blog.ai.api.vo.req.ChatHistoryReq;
import com.xzf.blog.ai.api.vo.req.ChatReq;
import com.xzf.blog.ai.api.vo.req.RagasReq;
import com.xzf.blog.ai.api.vo.resp.ChatHistoryRespVO;
import com.xzf.blog.ai.api.vo.resp.RagasRespVO;
import com.xzf.blog.ai.service.AiChatService;
import com.xzf.blog.framework.commons.response.PageResponse;
import com.xzf.blog.framework.commons.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class IAiChatApiServiceImpl implements IAiChatApiService {

    @Autowired
    private AiChatService aiChatService;


    @Override
    public Flux<String> streamingChat(ChatReq message) {
        return aiChatService.streamingChat(message);
    }

    @Override
    public PageResponse<ChatHistoryRespVO> getHistoryChat(ChatHistoryReq req) {
        return aiChatService.getHistoryChat(req);
    }

    @Override
    public Response<RagasRespVO> query(RagasReq req) {
        return aiChatService.ragasQuery(req);
    }
}
