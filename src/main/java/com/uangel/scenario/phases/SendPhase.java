package com.uangel.scenario.phases;

import lombok.Getter;
import org.w3c.dom.Node;

/**
 * @author dajin kim
 */
@Getter
public class SendPhase extends OutgoingPhase {
    private final String targetQueue;

    public SendPhase(Node xmlNode, int idx, boolean isProtoMode) {
        super(xmlNode, idx, isProtoMode);
        this.targetQueue = getTargetAttrValue();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Send{msgName:").append(msgName).append(", ");
        for (int i = 0; i < headerBodyInfos.size(); i++) {
            sb.append("Node").append(i+1).append(headerBodyInfos.get(i));
            if (i != (headerBodyInfos.size() - 1))
                sb.append(", ");
        }
/*        for (HeaderBodyInfo headerBody : headerBodyInfos) {
            sb.append(headerBody).append(", ");
        }*/
        sb.append("}");
        return sb.toString();
    }
}
