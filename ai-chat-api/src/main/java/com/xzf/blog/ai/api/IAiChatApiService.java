package com.xzf.blog.ai.api;

import com.xzf.blog.ai.api.vo.req.ChatHistoryReq;
import com.xzf.blog.ai.api.vo.req.ChatReq;
import com.xzf.blog.ai.api.vo.req.RagasReq;
import com.xzf.blog.ai.api.vo.resp.ChatHistoryRespVO;
import com.xzf.blog.ai.api.vo.resp.RagasRespVO;
import com.xzf.blog.framework.commons.response.PageResponse;
import com.xzf.blog.framework.commons.response.Response;
import com.xzf.framework.biz.context.aspect.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

public interface IAiChatApiService {

    /**
     * 流式智能对话
     * @param message
     * @return
     */
    @PreAuthorize
    @PostMapping(value = "/chat/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> streamingChat(@RequestBody ChatReq message);

    @PreAuthorize
    @PostMapping("/chat/history/get")
    PageResponse<ChatHistoryRespVO> getHistoryChat(@RequestBody ChatHistoryReq req);

    /**
     * Ragas评测用
     * @param req
     * @return
     */
    @PostMapping("/chat/ragas/query")
    Response<RagasRespVO> query(@RequestBody RagasReq req);

}
