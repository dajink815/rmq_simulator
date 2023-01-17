package com.uangel.scenario.phases;

import com.uangel.scenario.base.FieldInfo;
import com.uangel.scenario.base.MsgInfo;
import com.uangel.util.XmlUtil;
import lombok.ToString;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dajin kim
 */
@ToString
public class SendPhase extends MsgPhase {
    private final String className;
    private final List<MsgInfo> msgInfos = new ArrayList<>();

    public SendPhase(Node xmlNode, int idx) {
        super(xmlNode, idx);
        this.className = getStrAttrValue(xmlNode, "class");

        Element sendEle = (Element) xmlNode;
        NodeList msgList = sendEle.getChildNodes();

        for (int i = 0; i < msgList.getLength(); i++) {
            Node msgNode = msgList.item(i);
            if (isElementNode(msgNode)) {
                MsgInfo msgInfo = new MsgInfo();
                msgInfo.setClassName(getStrAttrValue(msgNode, "class"));

                Element msgEle = (Element) msgNode;
                NodeList fieldList = msgEle.getChildNodes();

                List<FieldInfo> fieldInfos = new ArrayList<>();
                for (int j = 0; j < fieldList.getLength(); j++) {
                    Node fieldNode = fieldList.item(j);
                    if (isElementNode(fieldNode)) {
                        NamedNodeMap fieldAttr = fieldNode.getAttributes();
                        String name = getStrAttrValue(fieldAttr, "name");
                        String type = getStrAttrValue(fieldAttr, "type");
                        String value = getStrAttrValue(fieldAttr, "value");
                        String exec = getStrAttrValue(fieldAttr, "exec");

                        FieldInfo fieldInfo = new FieldInfo(name, type, value, exec);
                        fieldInfos.add(fieldInfo);
                    }
                }

                msgInfo.setFieldInfos(fieldInfos);
                msgInfos.add(msgInfo);
            }
        }
    }
}
