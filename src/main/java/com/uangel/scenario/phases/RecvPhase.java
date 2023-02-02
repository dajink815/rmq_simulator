package com.uangel.scenario.phases;

import com.uangel.scenario.type.AttrName;
import com.uangel.util.StringUtil;
import lombok.Getter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author dajin kim
 */
@Getter
public class RecvPhase extends MsgPhase {
    private String msgName;
    private final String className;
    private final Boolean optional;
    private final String next;

    public RecvPhase(Node xmlNode, int idx) {
        super(xmlNode, idx);
        this.className = getClassAttrValue();
        this.optional = getBoolAttrWithDefault(AttrName.OPTIONAL.getValue(), false);
        this.next = getStrAttrValue(AttrName.NEXT.getValue());

        Element recvEle = (Element) xmlNode;
        NodeList msgList = recvEle.getChildNodes();

        for (int i = 0; i < msgList.getLength(); i++) {
            Node msgNode = msgList.item(i);
            if (isElementNode(msgNode) && StringUtil.isNull(msgName) && isBodyNode(msgNode)) {
                msgName = getClassAttrValue(msgNode);
                break;
            }

        }

    }

    @Override
    public String toString() {
        if (StringUtil.isNull(next)) {
            return "Recv{" + msgName +
                    "(" + optional +
                    ")}";
        } else {
            return "Recv{" + msgName +
                    "(" + optional +
                    "/next=" + next +
                    ")}";
        }

    }
}
