package com.zzx.matchlens.common;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum EventType {
    // 篮球
    SCORE("SCORE"),
    FOUL("FOUL"),
    ASSIST("ASSIST"),
    REBOUND("REBOUND"),
    STEAL("STEAL"),
    TURNOVER("TURNOVER"),
    TIMEOUT("TIMEOUT"),

    // 足球
    YELLOW_CARD("YELLOW_CARD"),
    RED_CARD("RED_CARD"),
    SUBSTITUTION("SUBSTITUTION"),

    // 排球
    BLOCK("BLOCK"),
    SERVE_ACE("SERVE_ACE"),
    ERROR("ERROR");

    @EnumValue
    private final String code;

    EventType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
