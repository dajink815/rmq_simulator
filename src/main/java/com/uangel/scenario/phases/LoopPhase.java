package com.uangel.scenario.phases;

import com.uangel.scenario.type.AttrName;
import lombok.Getter;
import org.w3c.dom.Node;

/**
 * @author dajin kim
 */
@Getter
public class LoopPhase extends OutgoingPhase {
    private final int reTrans;

    public LoopPhase(Node xmlNode, int idx) {
        super(xmlNode, idx);
        this.reTrans = getIntAttrWithDefault(AttrName.RETRANS.getValue(), 0);
    }

    @Override
    public String toString() {
        return "Loop{" + msgName +
                ", headerBody=" + headerBodyInfos +
                '}';
    }}
