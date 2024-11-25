package com.uangel.scenario.phases;

import com.uangel.scenario.model.FieldInfo;
import com.uangel.scenario.model.HeaderBodyInfo;
import com.uangel.util.StringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dajin kim
 */
@Slf4j
@Getter
public class OutgoingPhase extends MsgPhase {
    protected String msgName;
    protected final String className;
    private final String targetQueue;
    protected final List<HeaderBodyInfo> headerBodyInfos = new ArrayList<>();

    protected OutgoingPhase(Node xmlNode, int idx, boolean isProtoMode) {
        super(xmlNode, idx, isProtoMode);
        this.className = getClassAttrValue();
        this.targetQueue = getTargetAttrValue();

        NodeList headerBodyList = xmlNode.getChildNodes();

        for (int i = 0; i < headerBodyList.getLength(); i++) {
            Node headerBodyNode = headerBodyList.item(i);
            if (isElementNode(headerBodyNode)) {
                // get Class Attribute
                String classAttr = getClassAttrValue(headerBodyNode);
                String nameAttr = getNameAttrValue(headerBodyNode);
                if(nameAttr == null) {
                    nameAttr = (isProtoMode && classAttr.contains("_")) ? StringUtil.snakeToCamel(classAttr.toLowerCase()) : classAttr;
                }

                // Set Phase's Message Name
                if (StringUtil.isNull(msgName) && isBodyNode(headerBodyNode)) {
                    msgName = classAttr;
                }

                // Field List
                List<FieldInfo> fieldInfos = createFieldInfos(headerBodyNode.getChildNodes());
                // Create & Add HeaderBodyInfo
                HeaderBodyInfo headerBodyInfo = new HeaderBodyInfo(headerBodyNode, classAttr, nameAttr, fieldInfos, isProtoMode);
                headerBodyInfos.add(headerBodyInfo);
            }
        }

        log.debug("OutgoingPhase : [{}]", headerBodyInfos);
    }
}
