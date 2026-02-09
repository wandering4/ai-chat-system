package com.xzf.blog.ai.config;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class AgentTools {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Tool(name = "getToday",value = "获取当前时间")
    public String getToday() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }


}
