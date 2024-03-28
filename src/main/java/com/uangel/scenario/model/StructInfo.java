package com.uangel.scenario.model;

import com.uangel.scenario.XmlParser;
import com.uangel.scenario.type.AttrName;
import com.uangel.util.StringUtil;
import lombok.Getter;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author dajin kim
 */
@Getter
public class StructInfo extends XmlParser {

    private final List<FieldInfo> fieldInfos;
    private final String name;
    private final String className;
    private final List<StructInfo> structList;
    private boolean isRepeated = false;

    public StructInfo(Node structNode, boolean isProtoMode) {
        super(structNode, isProtoMode);
        String nameAttr = getStrAttrValue(AttrName.NAME.getValue());
        this.name = (isProtoMode && nameAttr.contains("_")) ? StringUtil.snakeToCamel(nameAttr.toLowerCase()) : nameAttr;
        this.className = getStrAttrValue(AttrName.CLASS.getValue());
        this.fieldInfos = createFieldInfos(structNode.getChildNodes());
        // 혹시 하위 struct node 있으면 또 파싱
        this.structList = createStructList(structNode.getChildNodes());
        String repeated = getStrAttrValue(AttrName.REPEATED.getValue());
        if (repeated != null && !repeated.isEmpty()) {
            this.isRepeated = repeated.equalsIgnoreCase("true");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{name=").append(name)
                .append("(").append(className).append(")")
                .append(" fields=").append(fieldInfos).append("}");

        if (!structList.isEmpty()) {
            sb.append("<ChildStruct").append(structList).append(">");
        }
        return sb.toString();
    }
}
