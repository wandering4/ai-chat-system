package com.xzf.blog.ai.api.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class RagasRespVO {

    private String answer;

    private List<String> contexts;

}
