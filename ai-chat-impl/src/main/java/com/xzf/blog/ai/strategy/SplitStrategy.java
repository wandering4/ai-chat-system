package com.xzf.blog.ai.strategy;

public interface SplitStrategy {
    String[] split(String article);
    String getName();
}
