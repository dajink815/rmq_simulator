package com.uangel.model;

import com.uangel.util.StringUtil;

/**
 * @author dajin kim
 */
public enum SimType {
    JSON("json"), PROTO("proto");

    private final String value;

    SimType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SimType getTypeEnum(String type) {
        switch (StringUtil.blankIfNull(type).toLowerCase()) {
            case "proto": case "p" :
                return PROTO;
            case "json": case "j" :
            default:
                return JSON;
        }
    }
}
