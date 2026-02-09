package com.xzf.blog.ai.commons.enums;

public enum ChatType {

    USER_MESSAGE(0, "user"),
    AI_MESSAGE(1, "ai");

    private final int code;
    private final String desc;

    ChatType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ChatType getByCode(int code) {
        for (ChatType type : ChatType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

}
