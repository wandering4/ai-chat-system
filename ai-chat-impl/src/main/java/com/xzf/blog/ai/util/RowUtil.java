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

    /**
     * 默认换行符模式（支持所有常见换行符）
     */
    public static final String LINE_SEPARATOR_PATTERN = "\r?\n|\r";

    /**
     * 系统换行符
     */
    public static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();

    /**
     * 常见换行符枚举
     */
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

    /**
     * 切割模式枚举
     */
    public enum SplitMode {
        /**
         * 包含空行
         */
        INCLUDE_EMPTY_LINES,
        /**
         * 去除空行
         */
        REMOVE_EMPTY_LINES,
        /**
         * 去除首尾空行
         */
        TRIM_EMPTY_LINES,
        /**
         * 去除所有空白行（包括只包含空白字符的行）
         */
        REMOVE_BLANK_LINES
    }

    /**
     * 按行分割文本（默认模式：包含空行）
     *
     * @param text 要分割的文本
     * @return 行数组
     */
    public static String[] split(String text) {
        return split(text, SplitMode.INCLUDE_EMPTY_LINES, null);
    }

    /**
     * 按行分割文本
     *
     * @param text 要分割的文本
     * @param mode 分割模式
     * @return 行数组
     */
    public static String[] split(String text, SplitMode mode) {
        return split(text, mode, null);
    }

    /**
     * 按行分割文本
     *
     * @param text 要分割的文本
     * @param customPattern 自定义分割正则表达式
     * @return 行数组
     */
    public static String[] split(String text, String customPattern) {
        return split(text, SplitMode.INCLUDE_EMPTY_LINES, customPattern);
    }

    /**
     * 按行分割文本
     *
     * @param text 要分割的文本
     * @param mode 分割模式
     * @param customPattern 自定义分割正则表达式
     * @return 行数组
     */
    public static String[] split(String text, SplitMode mode, String customPattern) {
        if (text == null) {
            return new String[0];
        }

        String pattern = (customPattern != null) ? customPattern : LINE_SEPARATOR_PATTERN;
        String[] lines = text.split(pattern, -1);  // 使用-1保留尾部的空字符串

        return processLinesByMode(lines, mode);
    }

    /**
     * 按行分割文本到List（默认模式）
     *
     * @param text 要分割的文本
     * @return 行列表
     */
    public static List<String> splitToList(String text) {
        return splitToList(text, SplitMode.INCLUDE_EMPTY_LINES, null);
    }

    /**
     * 按行分割文本到List
     *
     * @param text 要分割的文本
     * @param mode 分割模式
     * @return 行列表
     */
    public static List<String> splitToList(String text, SplitMode mode) {
        return splitToList(text, mode, null);
    }

    /**
     * 按行分割文本到List
     *
     * @param text 要分割的文本
     * @param mode 分割模式
     * @param customPattern 自定义分割正则表达式
     * @return 行列表
     */
    public static List<String> splitToList(String text, SplitMode mode, String customPattern) {
        String[] lines = split(text, mode, customPattern);
        List<String> result = new ArrayList<>(lines.length);
        for (String line : lines) {
            result.add(line);
        }
        return result;
    }

    /**
     * 流式处理文本行
     *
     * @param text 要分割的文本
     * @return 行流
     */
    public static Stream<String> splitToStream(String text) {
        return splitToStream(text, SplitMode.INCLUDE_EMPTY_LINES, null);
    }

    /**
     * 流式处理文本行
     *
     * @param text 要分割的文本
     * @param mode 分割模式
     * @return 行流
     */
    public static Stream<String> splitToStream(String text, SplitMode mode) {
        return splitToStream(text, mode, null);
    }

    /**
     * 流式处理文本行
     *
     * @param text 要分割的文本
     * @param mode 分割模式
     * @param customPattern 自定义分割正则表达式
     * @return 行流
     */
    public static Stream<String> splitToStream(String text, SplitMode mode, String customPattern) {
        String pattern = (customPattern != null) ? customPattern : LINE_SEPARATOR_PATTERN;

        if (text == null) {
            return Stream.empty();
        }

        Stream<String> stream = Pattern.compile(pattern)
                .splitAsStream(text)
                .sequential();  // 保持顺序

        return applyModeToStream(stream, mode);
    }

    /**
     * 高性能分割大文本（避免正则开销）
     *
     * @param text 要分割的文本
     * @return 行列表
     */
    public static List<String> splitLargeText(String text) {
        return splitLargeText(text, SplitMode.INCLUDE_EMPTY_LINES);
    }

    /**
     * 高性能分割大文本
     *
     * @param text 要分割的文本
     * @param mode 分割模式
     * @return 行列表
     */
    public static List<String> splitLargeText(String text, SplitMode mode) {
        if (text == null) {
            return new ArrayList<>();
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                // Unix/Linux/Mac 换行
                lines.add(currentLine.toString());
                currentLine.setLength(0);
            } else if (c == '\r') {
                // Windows 或 Classic Mac 换行
                lines.add(currentLine.toString());
                currentLine.setLength(0);

                // 如果是 Windows 的 \r\n，跳过下一个 \n
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
            } else {
                currentLine.append(c);
            }
        }

        // 添加最后一行
        lines.add(currentLine.toString());

        return processLinesListByMode(lines, mode);
    }




    /**
     * 合并行到单个字符串
     *
     * @param lines 行列表
     * @return 合并后的文本
     */
    public static String joinLines(List<String> lines) {
        return joinLines(lines, SYSTEM_LINE_SEPARATOR);
    }

    /**
     * 合并行到单个字符串
     *
     * @param lines 行列表
     * @param separator 行分隔符
     * @return 合并后的文本
     */
    public static String joinLines(List<String> lines, String separator) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }

        return String.join(separator, lines);
    }

    /**
     * 获取行数
     *
     * @param text 文本
     * @return 行数
     */
    public static int getLineCount(String text) {
        return getLineCount(text, SplitMode.INCLUDE_EMPTY_LINES);
    }

    /**
     * 获取行数
     *
     * @param text 文本
     * @param mode 分割模式
     * @return 行数
     */
    public static int getLineCount(String text, SplitMode mode) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        String[] lines = RowUtil.split(text, mode);
        return lines.length;
    }

    /**
     * 检测文本中的换行符类型
     *
     * @param text 文本
     * @return 换行符类型
     */
    public static LineSeparator detectLineSeparator(String text) {
        return LineSeparator.detect(text);
    }

    /**
     * 移除行首行尾空白字符
     *
     * @param lines 行列表
     * @return 处理后的行列表
     */
    public static List<String> trimLines(List<String> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }

        return lines.stream()
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * 移除行号
     *
     * @param text 文本
     * @param keepLineNumbers 是否保留行号
     * @return 处理后的文本
     */
    public static String processLineNumbers(String text, boolean keepLineNumbers) {
        if (!keepLineNumbers) {
            return text.replaceAll("^\\s*\\d+\\.?\\s*", "");
        }
        return text;
    }

    // 私有辅助方法

    private static String[] processLinesByMode(String[] lines, SplitMode mode) {
        if (mode == null || mode == SplitMode.INCLUDE_EMPTY_LINES) {
            return lines;
        }

        List<String> result = new ArrayList<>();
        boolean firstNonEmpty = false;
        boolean lastNonEmpty = false;
        int firstNonEmptyIndex = -1;
        int lastNonEmptyIndex = -1;

        // 找到第一个和最后一个非空行
        for (int i = 0; i < lines.length; i++) {
            if (!isBlankLine(lines[i])) {
                if (!firstNonEmpty) {
                    firstNonEmpty = true;
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
                    if (firstNonEmptyIndex != -1 &&
                            i >= firstNonEmptyIndex &&
                            i <= lastNonEmptyIndex) {
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

        return lines.stream()
                .filter(line -> {
                    switch (mode) {
                        case REMOVE_EMPTY_LINES:
                            return !line.isEmpty();
                        case REMOVE_BLANK_LINES:
                            return !isBlankLine(line);
                        case TRIM_EMPTY_LINES:
                            return true; // 在外部处理
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
                return stream; // 注意：流式处理中TRIM模式较难实现
            default:
                return stream;
        }
    }

    private static boolean isBlankLine(String line) {
        return line != null && line.trim().isEmpty();
    }

    // 示例用法
    public static void main(String[] args) {
        String text = "第一行\n第二行\r\n第三行\r第四行\n\n第六行";

        System.out.println("=== 原始文本 ===");
        System.out.println(text);

        System.out.println("\n=== 按默认模式分割 ===");
        String[] lines1 = RowUtil.split(text);
        for (int i = 0; i < lines1.length; i++) {
            System.out.printf("行 %d: '%s'%n", i + 1, lines1[i]);
        }

        System.out.println("\n=== 移除空行 ===");
        List<String> lines2 = splitToList(text, SplitMode.REMOVE_EMPTY_LINES);
        lines2.forEach(line -> System.out.println("行: " + line));

        System.out.println("\n=== 检测换行符类型 ===");
        LineSeparator separator = detectLineSeparator(text);
        System.out.println("换行符类型: " + separator.getDescription());

        System.out.println("\n=== 获取行数 ===");
        int count = getLineCount(text, SplitMode.REMOVE_EMPTY_LINES);
        System.out.println("非空行数: " + count);

        System.out.println("\n=== 流式处理 ===");
        splitToStream(text, SplitMode.REMOVE_BLANK_LINES)
                .forEach(line -> System.out.println("处理: " + line));
    }
}
