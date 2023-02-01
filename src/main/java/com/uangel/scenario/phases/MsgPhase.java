package com.uangel.scenario.phases;

import com.uangel.scenario.model.FieldInfo;
import com.uangel.scenario.type.AttrName;
import com.uangel.scenario.type.FieldType;
import com.uangel.scenario.type.NodeName;
import com.uangel.util.XmlUtil;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dajin kim
 */
@ToString
@Getter
public abstract class MsgPhase {
    private final int idx;
    private final NamedNodeMap attr;

    protected MsgPhase(Node xmlNode, int idx) {
        this.idx = idx;
        this.attr = xmlNode.getAttributes();
    }

    protected String getClassAttrValue() {
        return getStrAttrValue(AttrName.CLASS.getValue());
    }
    protected String getClassAttrValue(Node node) {
        NamedNodeMap nodeAttr = node.getAttributes();
        return XmlUtil.getStrParam(nodeAttr.getNamedItem(AttrName.CLASS.getValue()));
    }

    protected String getStrAttrValue(String name) {
        return XmlUtil.getStrParam(attr.getNamedItem(name));
    }
    protected String getStrAttrValue(NamedNodeMap attr, String name) {
        return XmlUtil.getStrParam(attr.getNamedItem(name));
    }

    protected int getIntAttrValue(String name) {
        return XmlUtil.getIntParam(attr.getNamedItem(name));
    }
    protected int getIntAttrWithDefault(String name, int defaultVal) {
        return XmlUtil.getIntParamWithDefault(attr.getNamedItem(name), defaultVal);
    }

    protected Boolean getBoolAttrValue(String name) {
        return XmlUtil.getBoolParam(attr.getNamedItem(name));
    }
    protected Boolean getBoolAttrWithDefault(String name, boolean defaultVal) {
        return XmlUtil.getBoolParamWithDefault(attr.getNamedItem(name), defaultVal);
    }

    protected boolean isElementNode(Node node) {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }

    protected boolean isBodyNode(Node node) {
        NodeName nodeName = NodeName.getNodeName(node.getNodeName());
        return NodeName.BODY.equals(nodeName);
    }

    protected List<FieldInfo> createFieldInfos(NodeList fieldList) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (int j = 0; j < fieldList.getLength(); j++) {
            Node fieldNode = fieldList.item(j);
            if (isElementNode(fieldNode)) {
                NamedNodeMap fieldAttr = fieldNode.getAttributes();
                String name = getStrAttrValue(fieldAttr, AttrName.NAME.getValue());
                String typeStr = getStrAttrValue(fieldAttr, AttrName.TYPE.getValue());
                String value = getStrAttrValue(fieldAttr, AttrName.VALUE.getValue());
                String exec = getStrAttrValue(fieldAttr, AttrName.EXEC.getValue());

                //todo name, type 필수 값 체크
                // value exec 둘 중 하나는 있는지 체크

                FieldType type = FieldType.getTypeEnum(typeStr);
                FieldInfo fieldInfo = new FieldInfo(name, type, value, exec);
                fieldInfos.add(fieldInfo);
            }
        }

        return fieldInfos;
    }
}
