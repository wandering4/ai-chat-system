package com.xzf.blog.ai.strategy;

import com.xzf.blog.ai.commons.constant.SplitStrategyConstants;
import com.xzf.blog.ai.util.RowUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RowSplitStrategyTest {

    private final RowSplitStrategy strategy = new RowSplitStrategy();

    @Test
    void shouldReturnExpectedStrategyName() {
        assertEquals(SplitStrategyConstants.ROW, strategy.getName());
    }

    @Test
    void shouldDelegateSplitToRowUtil() {
        String article = "line1\nline2";

        assertArrayEquals(RowUtil.split(article), strategy.split(article));
    }
}
