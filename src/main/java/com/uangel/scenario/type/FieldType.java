package com.uangel.scenario.type;

import com.uangel.util.StringUtil;

/**
 * @author dajin kim
 */
public enum FieldType {
    STR("str"), INT("int"), LONG("long"), BOOL("bool");

    private final String value;

    FieldType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FieldType getTypeEnum(String type) {
        switch (StringUtil.blankIfNull(type).toLowerCase()) {
            case "int":
            case "integer":
            case "i":
                return INT;
            case "long":
            case "l":
                return LONG;
            case "bool":
            case "boolean":
            case "b":
                return BOOL;
            case "str":
            case "string":
            case "s":
            default:
                return STR;
        }
    }
}
