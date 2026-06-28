package com.zzx.matchlens.common;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum MatchStatus {
    NOT_STARTED("NOT_STARTED"),
    RUNNING("RUNNING"),
    PAUSED("PAUSED"),
    FINISHED("FINISHED");

    @EnumValue
    private final String code;

    MatchStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
