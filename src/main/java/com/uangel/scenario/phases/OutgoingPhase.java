package com.uangel.scenario.phases;

import com.uangel.scenario.model.FieldInfo;
import com.uangel.scenario.model.HeaderBodyInfo;
import com.uangel.util.StringUtil;
import lombok.Getter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dajin kim
 */
@Getter
public class OutgoingPhase extends MsgPhase {
    protected String msgName;
    protected final String className;
    protected final List<HeaderBodyInfo> headerBodyInfos = new ArrayList<>();

    protected OutgoingPhase(Node xmlNode, int idx, boolean isProtoMode) {
        super(xmlNode, idx, isProtoMode);
        this.className = getClassAttrValue();

        NodeList headerBodyList = xmlNode.getChildNodes();

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
                List<FieldInfo> fieldInfos = createFieldInfos(headerBodyNode.getChildNodes());
                // Create & Add HeaderBodyInfo
                HeaderBodyInfo headerBodyInfo = new HeaderBodyInfo(headerBodyNode, msgClass, fieldInfos, isProtoMode);
                headerBodyInfos.add(headerBodyInfo);
            }
        }
    }
}
