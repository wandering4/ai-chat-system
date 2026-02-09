package com.xzf.blog.ai.commons.constant;

public interface RedisConstants {

    String CHAT_CONVERSATION_KEY = "chat:conversation:key:";

    public static String getChatConversationKey(Long userId, String conversationKey) {
        return CHAT_CONVERSATION_KEY + userId + "_" + conversationKey;
    }
}
