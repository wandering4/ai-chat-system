package com.xzf.blog.ai.strategy;

import com.xzf.blog.ai.commons.constant.SplitStrategyConstants;
import com.xzf.blog.ai.util.SlidingWindowUtil;
import org.springframework.stereotype.Component;

@Component
public class SlidingWindowSplitStrategy implements SplitStrategy{
    @Override
    public String[] split(String article) {
        return SlidingWindowUtil.split(article);
    }

    @Override
    public String getName() {
        return SplitStrategyConstants.SLIDING_WINDOW;
    }
}
