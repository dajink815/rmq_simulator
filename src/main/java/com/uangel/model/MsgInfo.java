package com.uangel.model;

import com.uangel.scenario.type.PhaseType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dajin kim
 */
@Getter
@Setter
@ToString
public class MsgInfo {
    private String msgName;
    private PhaseType phaseType;
    private final AtomicInteger count = new AtomicInteger();

    // Statistics

    public MsgInfo(String msgName, PhaseType phaseType) {
        this.msgName = msgName;
        this.phaseType = phaseType;
    }
}
