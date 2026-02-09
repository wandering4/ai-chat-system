package com.xzf.blog.ai.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 滑动窗口文本切分工具
 * 用于将长文本按固定窗口大小切分，支持窗口重叠以保持上下文连贯性
 */
public class SlidingWindowUtil {

    /**
     * 窗口大小（字符数）
     */
    private static final int WINDOW_SIZE = 400;

    /**
     * 滑动步长（字符数），小于窗口大小可产生重叠
     * 重叠部分 = WINDOW_SIZE - STEP_SIZE = 100 字符
     */
    private static final int STEP_SIZE = 300;

    /**
     * 使用默认参数切分文本
     *
     * @param text 待切分的文本
     * @return 切分后的文本片段数组
     */
    public static String[] split(String text) {
        return split(text, WINDOW_SIZE, STEP_SIZE);
    }

    /**
     * 使用自定义参数切分文本
     *
     * @param text       待切分的文本
     * @param windowSize 窗口大小（字符数）
     * @param stepSize   滑动步长（字符数）
     * @return 切分后的文本片段数组
     */
    public static String[] split(String text, int windowSize, int stepSize) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        // 预处理：去除多余空白字符
        text = text.replaceAll("\\s+", " ").trim();

        // 如果文本长度小于窗口大小，直接返回整个文本
        if (text.length() <= windowSize) {
            return new String[]{text};
        }

        List<String> segments = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + windowSize, text.length());
            String segment = text.substring(start, end).trim();

            // 只添加非空片段
            if (!segment.isEmpty()) {
                segments.add(segment);
            }

            // 如果已经到达文本末尾，退出循环
            if (end >= text.length()) {
                break;
            }

            start += stepSize;
        }

        return segments.toArray(new String[0]);
    }

    public static void main(String[] args) {
        String article = "数月来，美国间谍一直在监视委内瑞拉总统尼古拉斯·马杜罗（Nicolas Maduro'）的一举一动。\n" +
                "\n" +
                "一个小型团队，包括一个委内瑞拉政府内部消息源，持续观察这位63岁领导人睡在哪里、吃什么、穿什么，甚至据高级军官透露，还包括监视“他的宠物”。\n" +
                "\n" +
                "然后，在12月初，一项名为“绝对决心行动”（Operation Absolute Resolve）的计划最终敲定。这是数月精心策划和演练的结果，美国精锐部队甚至按马杜罗位于首度加拉加斯的安全屋搭建了一个全尺寸复制品，以练习进入路线。\n" +
                "\n" +
                "这项计划称得上是冷战以来美国在拉丁美洲前所未见的军事干预，它被严格保密。美国国会事前未被告知或咨询。随着细节确定，高层军官只需等待最佳时机发动行动。\n" +
                "\n" +
                "美国官员周六（1月3日）表示，他们希望最大化突袭效果。四天前曾有一次虚惊，当时总统特朗普已批准，但他们选择等待更好的天气和较少的云层。" +
                "\n" +
                "“从圣诞节到新年这几周，美国军人们一直待命，耐心等待触发条件达成，以及总统下令我们行动。”美国最高军事官员丹·凯恩将军（General Dan Caine）周六上午在新闻发布会上说。" +
                "“祝你好运，上帝保佑”\n" +
                "特朗普总统下令开始行动的时间，终于在周五美东时间22:46到来。“我们原本打算四天前、三天前、两天前就行动，然后突然时机成熟了。我们说：去吧。”特朗普本人在周六接受《福克斯与朋友们》（Fox & Friends）采访时说。\n" +
                "\n" +
                "“他对我们说祝你好运，上帝保佑。我们很感激。”凯恩将军说。特朗普的命令下达时，加拉加斯已接近午夜，给军方大半个夜晚的时间在黑暗中行动。";

        String[] split = split(article);
        for (String s : split) {
            System.out.println(s);
            System.out.println("========================");
        }
    }
}
