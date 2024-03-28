package com.uangel.scenario;

import com.uangel.scenario.model.FieldInfo;
import com.uangel.scenario.model.StructInfo;
import com.uangel.scenario.type.AttrName;
import com.uangel.scenario.type.FieldType;
import com.uangel.scenario.type.NodeName;
import com.uangel.util.StringUtil;
import com.uangel.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dajin kim
 */
@Slf4j
public class XmlParser {
    public static final Pattern initialSpaces = Pattern.compile("^\\s*");

    private final NamedNodeMap attr;
    private final boolean isProtoMode;

    public XmlParser(Node xmlNode, boolean isProtoMode) {
        this.attr = xmlNode.getAttributes();
        this.isProtoMode = isProtoMode;
    }

    protected String getClassAttrValue() {
        return getStrAttrValue(AttrName.CLASS.getValue());
    }
    protected String getClassAttrValue(Node node) {
        NamedNodeMap nodeAttr = node.getAttributes();
        return XmlUtil.getStrParam(nodeAttr.getNamedItem(AttrName.CLASS.getValue()));
    }

    protected String getNameAttrValue(Node node) {
        NamedNodeMap nodeAttr = node.getAttributes();
        return XmlUtil.getStrParam(nodeAttr.getNamedItem(AttrName.NAME.getValue()));
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

    // Field Node 파싱 -> FieldInfo 리스트 생성
    protected List<FieldInfo> createFieldInfos(NodeList fieldList) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (int j = 0; j < fieldList.getLength(); j++) {
            Node fieldNode = fieldList.item(j);
            if (isElementNode(fieldNode)) {
                String parentNodeName =  fieldNode.getParentNode().getNodeName();
                String fieldNodeName = fieldNode.getNodeName();
                if (!NodeName.FIELD.getValue().equals(fieldNodeName)) {
                    log.debug("[{}] child [{}] node pass createFieldInfos step", parentNodeName, fieldNodeName);
                    continue;
                }

                NamedNodeMap fieldAttr = fieldNode.getAttributes();
                String name = getStrAttrValue(fieldAttr, AttrName.NAME.getValue());
                String typeStr = getStrAttrValue(fieldAttr, AttrName.TYPE.getValue());
                String value = getStrAttrValue(fieldAttr, AttrName.VALUE.getValue());
                String exec = getStrAttrValue(fieldAttr, AttrName.EXEC.getValue());

                // name 필수
                if (StringUtil.isNull(name) ) {
                    log.warn("{}'s child [{}] - {} attribute is null, check scenario",
                            parentNodeName, fieldNodeName, AttrName.NAME.getValue());
                    continue;
                }

                // value exec 둘 중 하나는 있는지 체크
                if (StringUtil.isNull(value) && StringUtil.isNull(exec)) {
                    log.warn("{}'s child [{}:{}] - {} or {} attribute is null, check scenario",
                            parentNodeName, fieldNodeName, name, AttrName.VALUE.getValue(), AttrName.EXEC.getValue());
                    continue;
                }

                // Snake To Camel
                if (isProtoMode && name.contains("_")) {
                    String before = name;
                    name = StringUtil.snakeToCamel(name.toLowerCase());
                    log.debug("XmlParser [{}] Child Field... name:{} -> {}", fieldNode.getParentNode().getNodeName(), before, name);
                }

                // type 은 기본 타입 STR, 값 존재여부 체크 불필요
                FieldType type = FieldType.getTypeEnum(typeStr);
                FieldInfo fieldInfo = new FieldInfo(name, type, value, exec);
                fieldInfos.add(fieldInfo);
            }
        }

        return fieldInfos;
    }

    // Struct Node 파싱 -> StructInfo 리스트 생성
    protected List<StructInfo> createStructList(NodeList childNodes) {
        List<StructInfo> structList = new ArrayList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (isElementNode(childNode)
                    && NodeName.STRUCT.getValue().equals(childNode.getNodeName())) {
                StructInfo structInfo = new StructInfo(childNode, isProtoMode);
                structList.add(structInfo);
            }
        }
        return structList;
    }

    /**
     * @param message
     *            - the text node parsed out of the XML file
     * @return the SIP message stripped of formatting whitespace
     */
    public static String stripWhitespace(String message) {
        String[] lines = message.split("\r?\n", 0);
        StringBuilder sb = new StringBuilder();
        boolean firstNonEmptyLine = false;
        boolean seenNonEmptyLine = false;
        int spaces = 0;
        Pattern someSpaces = null;
        for (String line : lines) {
            // Skip over empty lines at the start
            if (!seenNonEmptyLine) {
                boolean isLineBlank = (line.trim().isEmpty());
                if (!isLineBlank) {
                    firstNonEmptyLine = true;
                    seenNonEmptyLine = true;
                }
            }

            // If the first line is indented, save off the indentation...
            if (firstNonEmptyLine) {
                Matcher m = initialSpaces.matcher(line);
                if (m.find()) {
                    spaces = m.group().length();
                    someSpaces = Pattern.compile("^\\s{0," + spaces + "}");
                }
                firstNonEmptyLine = false;
            }

            // ...and strip off at most that much whitespace from subsequent lines.
            if (seenNonEmptyLine) {
                if (someSpaces != null) {
                    sb.append(someSpaces.matcher(line).replaceFirst(""));
                } else {
                    sb.append(line);
                }
                sb.append("\r\n");
            }
        }

        // Remove trailing whitespace (to match the XML format) but then add
        // CRLF CRLF to end the message if there's no body
        String stringSoFar = sb.toString().trim();
        if (!stringSoFar.contains("\r\n\r\n")) {
            stringSoFar += "\r\n\r\n";
        }
        if (!stringSoFar.endsWith("\r\n")) stringSoFar += "\r\n";
        return stringSoFar;
    }
}
