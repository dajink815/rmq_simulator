package com.uangel.scenario.phases;

import com.uangel.scenario.model.*;
import com.uangel.util.StringUtil;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.Element;
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
        this.className = getClassAttrValue();

        Element sendEle = (Element) xmlNode;
        NodeList headerBodyList = sendEle.getChildNodes();

        for (int i = 0; i < headerBodyList.getLength(); i++) {
            Node headerBodyNode = headerBodyList.item(i);
            if (isElementNode(headerBodyNode)) {
                // get Class Attribute
                String msgClass = getClassAttrValue(headerBodyNode);

                // Set Phase's Message Name
                if (StringUtil.isNull(msgName) && isBodyNode(headerBodyNode)) {
                    msgName = msgClass;
                }

                // Field List
                Element headerBodyEle = (Element) headerBodyNode;
                List<FieldInfo> fieldInfos = createFieldInfos(headerBodyEle.getChildNodes());

                // Create & Add HeaderBodyInfo
                HeaderBodyInfo headerBodyInfo = new HeaderBodyInfo(msgClass, fieldInfos);
                headerBodyInfos.add(headerBodyInfo);
            }
        }
    }
}
