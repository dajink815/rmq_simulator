package com.uangel.scenario.phases;

import lombok.ToString;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author dajin kim
 */
@ToString
public class RecvPhase extends MsgPhase {
    private final String className;
    private String bodyClassName;

    public RecvPhase(Node xmlNode, int idx) {
        super(xmlNode, idx);
        this.className = getStrAttrValue(xmlNode, "class");

        Element recvEle = (Element) xmlNode;
        NodeList msgList = recvEle.getChildNodes();

        for (int i = 0; i < msgList.getLength(); i++) {
            Node msgNode = msgList.item(i);
            if (isElementNode( msgNode)) {
                bodyClassName = getStrAttrValue(msgNode, "class");
                break;
            }

        }

    }
}
