package com.uangel.scenario.phases.actions;

import lombok.Getter;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dajin kim
 */
@Getter
public class ActionPhase {
    public final List<ExecNode> execs =new ArrayList<>();
    public final int index;

    public ActionPhase(Node xmlNode, int index) {
        this.index = index;

        // todo Action 하위에 단일 Exec 노드로 처리?
        for (Node m = xmlNode.getFirstChild(); m != null; m = m.getNextSibling()) {
            if (m.getNodeName().equals("exec")){
                execs.add(new ExecNode(m));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Execs").append(index).append("(");
        for (int i = 0; i < execs.size(); i++) {
            sb.append(execs.get(i));
            if (i != (execs.size() - 1)) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}
