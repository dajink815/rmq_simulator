package com.uangel.scenario.phases;

import com.uangel.scenario.phases.actions.ActionPhase;
import lombok.Getter;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dajin kim
 */
@Getter
public class NopPhase extends MsgPhase {
    private final List<ActionPhase> actions = new ArrayList<>();

    public NopPhase(Node xmlNode, int idx) {
        super(xmlNode, idx);
        int index = 1;
        for (Node m = xmlNode.getFirstChild(); m != null; m = m.getNextSibling()) {
            if (m.getNodeName().equals("action")){
                actions.add(new ActionPhase(m, index));
                index++;
            }
        }
    }

    @Override
    public String toString() {
        int listSize = actions.size();
        StringBuilder sb = new StringBuilder("Nop(").append(listSize).append("){");
        for (int i = 0; i < listSize; i++) {
            sb.append(actions.get(i));
            if (i != (listSize - 1)) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }
}
