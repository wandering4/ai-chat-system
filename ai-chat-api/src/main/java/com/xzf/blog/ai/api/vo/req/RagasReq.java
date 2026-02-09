package com.xzf.blog.ai.api.vo.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagasReq {

    @NotBlank(message = "问题不能为空")
    private String query;

    private boolean hyde = false;
}
