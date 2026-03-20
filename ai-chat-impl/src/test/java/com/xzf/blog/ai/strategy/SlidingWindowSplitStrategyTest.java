package com.xzf.blog.ai.strategy;

import com.xzf.blog.ai.commons.constant.SplitStrategyConstants;
import com.xzf.blog.ai.util.SlidingWindowUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SlidingWindowSplitStrategyTest {

    private final SlidingWindowSplitStrategy strategy = new SlidingWindowSplitStrategy();

    @Test
    void shouldReturnExpectedStrategyName() {
        assertEquals(SplitStrategyConstants.SLIDING_WINDOW, strategy.getName());
    }

    @Test
    void shouldDelegateSplitToSlidingWindowUtil() {
        String article = "alpha beta gamma";

        assertArrayEquals(SlidingWindowUtil.split(article), strategy.split(article));
    }
}
