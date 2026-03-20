package com.xzf.blog.ai.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RowUtil {

    // 私有构造函数，防止实例化
    private RowUtil() {
        throw new AssertionError("工具类禁止实例化");
    }

    public static final String LINE_SEPARATOR_PATTERN = "\r?\n|\r";

    public static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();

    public enum LineSeparator {
        LF("\n", "LF (Unix/Linux/Mac)"),
        CR_LF("\r\n", "CRLF (Windows)"),
        CR("\r", "CR (Classic Mac)");

        private final String separator;
        private final String description;

        LineSeparator(String separator, String description) {
            this.separator = separator;
            this.description = description;
        }

        public String getSeparator() {
            return separator;
        }

        public String getDescription() {
            return description;
        }

        public static LineSeparator detect(String text) {
            if (text.contains("\r\n")) {
                return CR_LF;
            } else if (text.contains("\n")) {
                return LF;
            } else if (text.contains("\r")) {
                return CR;
            }
            return SYSTEM_LINE_SEPARATOR.equals("\r\n") ? CR_LF : LF;
        }
    }

    public enum SplitMode {
        INCLUDE_EMPTY_LINES,
        REMOVE_EMPTY_LINES,
        TRIM_EMPTY_LINES,
        REMOVE_BLANK_LINES
    }

    public static String[] split(String text) {
        return split(text, SplitMode.INCLUDE_EMPTY_LINES, null);
    }

    public static String[] split(String text, SplitMode mode) {
        return split(text, mode, null);
    }

    public static String[] split(String text, String customPattern) {
        return split(text, SplitMode.INCLUDE_EMPTY_LINES, customPattern);
    }

    public static String[] split(String text, SplitMode mode, String customPattern) {
        if (text == null) {
            return new String[0];
        }

        String pattern = customPattern != null ? customPattern : LINE_SEPARATOR_PATTERN;
        String[] lines = text.split(pattern, -1);
        return processLinesByMode(lines, mode);
    }

    public static List<String> splitToList(String text) {
        return splitToList(text, SplitMode.INCLUDE_EMPTY_LINES, null);
    }

    public static List<String> splitToList(String text, SplitMode mode) {
        return splitToList(text, mode, null);
    }

    public static List<String> splitToList(String text, SplitMode mode, String customPattern) {
        String[] lines = split(text, mode, customPattern);
        List<String> result = new ArrayList<>(lines.length);
        for (String line : lines) {
            result.add(line);
        }
        return result;
    }

    public static Stream<String> splitToStream(String text) {
        return splitToStream(text, SplitMode.INCLUDE_EMPTY_LINES, null);
    }

    public static Stream<String> splitToStream(String text, SplitMode mode) {
        return splitToStream(text, mode, null);
    }

    public static Stream<String> splitToStream(String text, SplitMode mode, String customPattern) {
        String pattern = customPattern != null ? customPattern : LINE_SEPARATOR_PATTERN;

        if (text == null) {
            return Stream.empty();
        }

        Stream<String> stream = Pattern.compile(pattern)
                .splitAsStream(text)
                .sequential();

        return applyModeToStream(stream, mode);
    }

    public static List<String> splitLargeText(String text) {
        return splitLargeText(text, SplitMode.INCLUDE_EMPTY_LINES);
    }

    public static List<String> splitLargeText(String text, SplitMode mode) {
        if (text == null) {
            return new ArrayList<>();
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
            } else if (c == '\r') {
                lines.add(currentLine.toString());
                currentLine.setLength(0);

                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
            } else {
                currentLine.append(c);
            }
        }

        lines.add(currentLine.toString());
        return processLinesListByMode(lines, mode);
    }

    public static String joinLines(List<String> lines) {
        return joinLines(lines, SYSTEM_LINE_SEPARATOR);
    }

    public static String joinLines(List<String> lines, String separator) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }

        return String.join(separator, lines);
    }

    public static int getLineCount(String text) {
        return getLineCount(text, SplitMode.INCLUDE_EMPTY_LINES);
    }

    public static int getLineCount(String text, SplitMode mode) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        return RowUtil.split(text, mode).length;
    }

    public static LineSeparator detectLineSeparator(String text) {
        return LineSeparator.detect(text);
    }

    public static List<String> trimLines(List<String> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }

        return lines.stream()
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static String processLineNumbers(String text, boolean keepLineNumbers) {
        if (!keepLineNumbers) {
            return text.replaceAll("^\\s*\\d+\\.?\\s*", "");
        }
        return text;
    }

    private static String[] processLinesByMode(String[] lines, SplitMode mode) {
        if (mode == null || mode == SplitMode.INCLUDE_EMPTY_LINES) {
            return lines;
        }

        List<String> result = new ArrayList<>();
        int firstNonEmptyIndex = -1;
        int lastNonEmptyIndex = -1;

        for (int i = 0; i < lines.length; i++) {
            if (!isBlankLine(lines[i])) {
                if (firstNonEmptyIndex == -1) {
                    firstNonEmptyIndex = i;
                }
                lastNonEmptyIndex = i;
            }
        }

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            boolean isEmpty = line.isEmpty();
            boolean isBlank = isBlankLine(line);

            switch (mode) {
                case REMOVE_EMPTY_LINES:
                    if (!isEmpty) {
                        result.add(line);
                    }
                    break;
                case REMOVE_BLANK_LINES:
                    if (!isBlank) {
                        result.add(line);
                    }
                    break;
                case TRIM_EMPTY_LINES:
                    if (firstNonEmptyIndex != -1 && i >= firstNonEmptyIndex && i <= lastNonEmptyIndex) {
                        result.add(line);
                    }
                    break;
                default:
                    result.add(line);
            }
        }

        return result.toArray(new String[0]);
    }

    private static List<String> processLinesListByMode(List<String> lines, SplitMode mode) {
        if (mode == null || mode == SplitMode.INCLUDE_EMPTY_LINES) {
            return new ArrayList<>(lines);
        }

        if (mode == SplitMode.TRIM_EMPTY_LINES) {
            int start = 0;
            int end = lines.size() - 1;

            while (start <= end && isBlankLine(lines.get(start))) {
                start++;
            }

            while (end >= start && isBlankLine(lines.get(end))) {
                end--;
            }

            if (start > end) {
                return new ArrayList<>();
            }

            return new ArrayList<>(lines.subList(start, end + 1));
        }

        return lines.stream()
                .filter(line -> {
                    switch (mode) {
                        case REMOVE_EMPTY_LINES:
                            return !line.isEmpty();
                        case REMOVE_BLANK_LINES:
                            return !isBlankLine(line);
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }

    private static Stream<String> applyModeToStream(Stream<String> stream, SplitMode mode) {
        if (mode == null || mode == SplitMode.INCLUDE_EMPTY_LINES) {
            return stream;
        }

        switch (mode) {
            case REMOVE_EMPTY_LINES:
                return stream.filter(line -> !line.isEmpty());
            case REMOVE_BLANK_LINES:
                return stream.filter(line -> !isBlankLine(line));
            case TRIM_EMPTY_LINES:
            default:
                return stream;
        }
    }

    private static boolean isBlankLine(String line) {
        return line != null && line.trim().isEmpty();
    }
}
