package com.xzf.blog.ai.service;

import com.xzf.blog.ai.api.vo.req.ArticleReq;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RAGService {

    public boolean uploadFile(List<MultipartFile> files);

    public boolean uploadArticle(ArticleReq req);

    public List<TextSegment> search(Long articleId,String query);

    public boolean deleteByArticleId(Long articleId);

}
