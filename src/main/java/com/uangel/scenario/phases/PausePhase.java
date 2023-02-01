package com.uangel.scenario.phases;

import com.uangel.scenario.type.AttrName;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.Node;

/**
 * @author dajin kim
 */
@ToString
@Getter
public class PausePhase extends MsgPhase {

    private final Integer milliSeconds;

    public PausePhase(Node xmlNode, int idx) {
        super(xmlNode, idx);
        this.milliSeconds = getIntAttrValue(AttrName.MILLIS.getValue());
    }
}
