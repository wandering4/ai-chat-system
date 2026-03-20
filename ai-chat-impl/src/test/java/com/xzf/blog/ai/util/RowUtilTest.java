package com.xzf.blog.ai.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class RowUtilTest {

    @Test
    void shouldSplitMixedLineSeparators() {
        String text = "first\nsecond\r\nthird\rfourth";

        assertArrayEquals(new String[]{"first", "second", "third", "fourth"}, RowUtil.split(text));
    }

    @Test
    void shouldRespectDifferentSplitModes() {
        String text = "\nalpha\n\n   \nbeta\n";

        assertArrayEquals(new String[]{"", "alpha", "", "   ", "beta", ""}, RowUtil.split(text, RowUtil.SplitMode.INCLUDE_EMPTY_LINES));
        assertArrayEquals(new String[]{"alpha", "   ", "beta"}, RowUtil.split(text, RowUtil.SplitMode.REMOVE_EMPTY_LINES));
        assertArrayEquals(new String[]{"alpha", "beta"}, RowUtil.split(text, RowUtil.SplitMode.REMOVE_BLANK_LINES));
        assertArrayEquals(new String[]{"alpha", "", "   ", "beta"}, RowUtil.split(text, RowUtil.SplitMode.TRIM_EMPTY_LINES));
    }

    @Test
    void shouldKeepTrimSemanticsConsistentForLargeTextSplit() {
        String text = "\nalpha\n\n   \nbeta\n";

        assertIterableEquals(List.of("alpha", "", "   ", "beta"), RowUtil.splitLargeText(text, RowUtil.SplitMode.TRIM_EMPTY_LINES));
    }

    @Test
    void shouldSupportListJoinCountAndDetectionHelpers() {
        String text = "1\r\n2\r\n3";

        assertIterableEquals(List.of("1", "2", "3"), RowUtil.splitToList(text));
        assertEquals("a|b", RowUtil.joinLines(List.of("a", "b"), "|"));
        assertEquals(1, RowUtil.getLineCount("a\n\n", RowUtil.SplitMode.REMOVE_EMPTY_LINES));
        assertEquals(RowUtil.LineSeparator.CR_LF, RowUtil.detectLineSeparator(text));
    }

    @Test
    void shouldTrimLinesAndProcessLineNumbers() {
        assertIterableEquals(List.of("a", "b"), RowUtil.trimLines(List.of(" a ", "\tb\t")));
        assertEquals("example", RowUtil.processLineNumbers("12. example", false));
        assertEquals("12. example", RowUtil.processLineNumbers("12. example", true));
    }
}
