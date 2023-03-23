package com.uangel.scenario.model;

import com.uangel.scenario.XmlParser;
import com.uangel.util.StringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * send, recv 하위 노드인 header, body 정보 포함하는 객체
 *
 * @author dajin kim
 */
@Getter
@Slf4j
public class HeaderBodyInfo extends XmlParser {

    private final String className;
    private final List<FieldInfo> fieldInfos;
    private final List<HeaderBodyInfo> bodyList = new ArrayList<>();
    private final List<StructInfo> structList;

    public HeaderBodyInfo(Node xmlNode, String className, List<FieldInfo> fieldInfos, boolean isProtoMode) {
        super(xmlNode, isProtoMode);
        this.className = (isProtoMode && className.contains("_")) ? StringUtil.snakeToCamel(className.toLowerCase()) : className;
        this.fieldInfos = fieldInfos;
        this.structList = createStructList(xmlNode.getChildNodes());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(").append(className).append("Class fields=").append(fieldInfos);
        if (!structList.isEmpty()) {
            sb.append(", childStruct").append(structList);
        }
        sb.append(")");
        return sb.toString();
    }
}
