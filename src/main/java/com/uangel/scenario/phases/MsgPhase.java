package com.uangel.scenario.phases;

import com.uangel.util.XmlUtil;
import lombok.ToString;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author dajin kim
 */
@ToString
public abstract class MsgPhase {
    private final int idx;

    protected MsgPhase(Node xmlNode, int idx) {
        this.idx = idx;
        NamedNodeMap attr = xmlNode.getAttributes();
    }

    public String getStrAttrValue(Node node, String name) {
        NamedNodeMap attr = node.getAttributes();
        return XmlUtil.getStrParam(attr.getNamedItem(name));
    }

    public String getStrAttrValue(NamedNodeMap attr, String name) {
        return XmlUtil.getStrParam(attr.getNamedItem(name));
    }

    public boolean isElementNode(Node node) {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }
}
