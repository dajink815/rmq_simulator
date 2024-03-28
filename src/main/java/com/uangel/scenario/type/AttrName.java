package com.uangel.scenario.type;

/**
 * @author dajin kim
 */
public enum AttrName {
    NAME("name"), CLASS("class"),
    TYPE("type"), VALUE("value"), EXEC("exec"),
    MILLIS("milliseconds"), RETRANS("retrans"),
    OPTIONAL("optional"), NEXT("next"),
    ID("id"), REPEATED("repeated");

    private final String value;

    AttrName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
