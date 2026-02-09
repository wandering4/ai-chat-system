package com.xzf.blog.ai.service;

import com.xzf.blog.ai.api.vo.req.ChatHistoryReq;
import com.xzf.blog.ai.api.vo.req.ChatReq;
import com.xzf.blog.ai.api.vo.req.RagasReq;
import com.xzf.blog.ai.api.vo.resp.ChatHistoryRespVO;
import com.xzf.blog.ai.api.vo.resp.RagasRespVO;
import com.xzf.blog.framework.commons.response.PageResponse;
import reactor.core.publisher.Flux;
import com.xzf.blog.framework.commons.response.Response;

public interface AiChatService {

    public Response<String> generateAbstract(String article) ;

    public Flux<String> streamingChat(ChatReq req);

    PageResponse<ChatHistoryRespVO> getHistoryChat(ChatHistoryReq req);

    Response<RagasRespVO> ragasQuery(RagasReq req);
}
