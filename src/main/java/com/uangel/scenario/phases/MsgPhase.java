package com.uangel.scenario.phases;

import com.uangel.scenario.type.AttrName;
import com.uangel.scenario.type.NodeName;
import com.uangel.util.XmlUtil;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author dajin kim
 */
@ToString
@Getter
public abstract class MsgPhase {
    private final int idx;

    protected MsgPhase(Node xmlNode, int idx) {
        this.idx = idx;
        NamedNodeMap attr = xmlNode.getAttributes();
    }

    protected String getClassAttrValue(Node node) {
        return getStrAttrValue(node, AttrName.CLASS.getValue());
    }
    protected String getStrAttrValue(Node node, String name) {
        NamedNodeMap attr = node.getAttributes();
        return XmlUtil.getStrParam(attr.getNamedItem(name));
    }

    protected String getStrAttrValue(NamedNodeMap attr, String name) {
        return XmlUtil.getStrParam(attr.getNamedItem(name));
    }

    protected boolean isElementNode(Node node) {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }

    protected boolean isBodyNode(Node node) {
        NodeName nodeName = NodeName.getNodeName(node.getNodeName());
        return NodeName.BODY.equals(nodeName);
    }
}
