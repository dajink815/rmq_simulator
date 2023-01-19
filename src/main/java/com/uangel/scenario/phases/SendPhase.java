package com.uangel.scenario.phases;

import com.uangel.scenario.model.*;
import com.uangel.scenario.type.AttrName;
import com.uangel.scenario.type.FieldType;
import com.uangel.util.StringUtil;
import lombok.Getter;
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
@Getter
public class SendPhase extends MsgPhase {
    private String msgName;
    private final String className;
    private final List<HeaderBodyInfo> headerBodyInfos = new ArrayList<>();

    public SendPhase(Node xmlNode, int idx) {
        super(xmlNode, idx);
        this.className = getClassAttrValue(xmlNode);

        Element sendEle = (Element) xmlNode;
        NodeList msgList = sendEle.getChildNodes();

        for (int i = 0; i < msgList.getLength(); i++) {
            Node msgNode = msgList.item(i);
            if (isElementNode(msgNode)) {
                String msgClass = getClassAttrValue(msgNode);
                HeaderBodyInfo headerBodyInfo = new HeaderBodyInfo();
                if (StringUtil.isNull(msgName) && isBodyNode(msgNode)) {
                    msgName = msgClass;
                }

                headerBodyInfo.setClassName(msgClass);

                Element msgEle = (Element) msgNode;
                NodeList fieldList = msgEle.getChildNodes();

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

                headerBodyInfo.setFieldInfos(fieldInfos);
                headerBodyInfos.add(headerBodyInfo);
            }
        }
    }
}
