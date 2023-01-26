package com.uangel.scenario;

import com.uangel.scenario.phases.MsgPhase;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author dajin kim
 */
@Slf4j
@ToString
public class Scenario {

    private final String name;
    private final List<MsgPhase> phases;
    private final List<String> msgNameList = new ArrayList<>();

    public Scenario(String name, List<MsgPhase> phases) {
        this.name = name;
        this.phases = phases;
        //this.setMsgNameList();
    }

    public List<MsgPhase> phases() {
        return Collections.unmodifiableList(this.phases);
    }

    public MsgPhase getPhase(int idx) {
        return this.phases.get(idx);
    }

    public String getName() {
        return this.name;
    }

}
