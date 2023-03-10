package com.uangel.scenario.phases;

import lombok.Getter;
import org.w3c.dom.Node;

/**
 * @author dajin kim
 */
@Getter
public class SendPhase extends OutgoingPhase {

    public SendPhase(Node xmlNode, int idx) {
        super(xmlNode, idx);
    }

    @Override
    public String toString() {
        return "Send{" + msgName +
                ", headerBody=" + headerBodyInfos +
                '}';
    }
}
