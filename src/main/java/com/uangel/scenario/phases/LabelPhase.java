package com.uangel.scenario.phases;

import com.uangel.scenario.type.AttrName;
import lombok.Getter;
import org.w3c.dom.Node;

/**
 * @author dajin kim
 */
@Getter
public class LabelPhase extends OutgoingPhase {
    private final String id;

    public LabelPhase(Node xmlNode, int idx, boolean isProtoMode) {
        super(xmlNode, idx, isProtoMode);
        this.id = getStrAttrValue(AttrName.ID.getValue());
    }

    @Override
    public String toString() {
        return "Label{msgName:" + msgName +
                ", headerBody=" + headerBodyInfos +
                '}';
    }
}
