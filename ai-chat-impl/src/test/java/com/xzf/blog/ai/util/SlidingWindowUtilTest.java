package com.xzf.blog.ai.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SlidingWindowUtilTest {

    @Test
    void shouldReturnEmptyArrayForNullOrEmptyText() {
        assertEquals(0, SlidingWindowUtil.split(null).length);
        assertEquals(0, SlidingWindowUtil.split("").length);
    }

    @Test
    void shouldReturnSingleSegmentWhenTextFitsWindow() {
        String[] result = SlidingWindowUtil.split("short text", 20, 10);

        assertArrayEquals(new String[]{"short text"}, result);
    }

    @Test
    void shouldNormalizeWhitespaceBeforeSplitting() {
        String[] result = SlidingWindowUtil.split("  alpha \n\n beta\tgamma  ", 50, 10);

        assertArrayEquals(new String[]{"alpha beta gamma"}, result);
    }

    @Test
    void shouldSplitByCustomWindowAndStepWithOverlap() {
        String[] result = SlidingWindowUtil.split("abcdefghij", 4, 3);

        assertArrayEquals(new String[]{"abcd", "defg", "ghij"}, result);
    }
}
