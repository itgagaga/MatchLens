package com.zzx.matchlens.common;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum SportType {
    BASKETBALL("BASKETBALL"),
    FOOTBALL("FOOTBALL"),
    VOLLEYBALL("VOLLEYBALL"),
    GENERAL("GENERAL");

    @EnumValue
    private final String code;

    SportType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
