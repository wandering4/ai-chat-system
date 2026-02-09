package com.xzf.blog.ai.controller;//package com.xzf.ai.chat.trigger.http;

import com.xzf.blog.ai.api.IRAGApiService;
import com.xzf.blog.ai.api.vo.req.ArticleReq;
import com.xzf.blog.ai.service.RAGService;
import com.xzf.blog.framework.commons.response.Response;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
public class IRAGApiServiceImpl implements IRAGApiService {

    @Autowired
    private RAGService ragService;

//    @Override
//    public Response<String> uploadFile(List<MultipartFile> files) {
//        boolean success = ragService.uploadFile(files);
//        if (success) {
//            return Response.success("上传成功");
//        }
//        return Response.fail("上传失败");
//    }
//
//    @Override
//    public Response<String> uploadArticle(ArticleReq req) {
//        boolean success = ragService.uploadArticle(req);
//        if (success) {
//            return Response.success("上传成功");
//        }
//        return Response.fail("上传失败");
//    }
}
