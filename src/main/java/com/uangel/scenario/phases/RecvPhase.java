package com.uangel.scenario.phases;

import com.uangel.util.StringUtil;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author dajin kim
 */
@Getter
@ToString
public class RecvPhase extends MsgPhase {
    private String msgName;
    private final String className;

    public RecvPhase(Node xmlNode, int idx) {
        super(xmlNode, idx);
        this.className = getClassAttrValue(xmlNode);

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
}
