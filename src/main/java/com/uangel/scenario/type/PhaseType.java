package com.uangel.scenario.type;

/**
 * @author dajin kim
 */
public enum PhaseType {

    SEND("Send"), RECV("Recv"),  PAUSE("Pause");

    private final String value;

    PhaseType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
