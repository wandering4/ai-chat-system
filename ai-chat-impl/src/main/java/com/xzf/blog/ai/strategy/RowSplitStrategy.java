package com.xzf.blog.ai.strategy;

import com.xzf.blog.ai.commons.constant.SplitStrategyConstants;
import com.xzf.blog.ai.util.RowUtil;
import com.xzf.blog.ai.util.SlidingWindowUtil;
import org.springframework.stereotype.Component;

@Component
public class RowSplitStrategy implements SplitStrategy{
    @Override
    public String[] split(String article) {
        return RowUtil.split(article);
    }

    @Override
    public String getName() {
        return SplitStrategyConstants.ROW;
    }
}
