package com.uangel.scenario.type;

import com.uangel.util.StringUtil;

/**
 * @author dajin kim
 */
public enum NodeName {
    SCENARIO("scenario"),
    SEND("send"), RECV("recv"), PAUSE("pause"),
    HEADER("header"), BODY("body"), FIELD("field");

    private final String value;

    NodeName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NodeName getNodeName(String type) {
        switch (StringUtil.blankIfNull(type).toLowerCase()) {
            case "scenario":
                return SCENARIO;
            case "send":
            case "s":
                return SEND;
            case "recv":
            case "r":
                return RECV;
            case "pause":
            case "p":
                return PAUSE;
            case "header":
            case "h":
                return HEADER;
            case "body":
            case "b":
                return BODY;
            case "field":
            case "f":
            default:
                return FIELD;
        }
    }

}
