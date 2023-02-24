package com.uangel.scenario.phases;

import com.uangel.scenario.model.FieldInfo;
import com.uangel.scenario.type.AttrName;
import com.uangel.scenario.type.FieldType;
import com.uangel.scenario.type.NodeName;
import com.uangel.util.StringUtil;
import com.uangel.util.XmlUtil;
import lombok.Getter;
import lombok.ToString;
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
@ToString
@Getter
@Slf4j
public abstract class MsgPhase {
    public static final Pattern initialSpaces = Pattern.compile("^\\s*");

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



                // name 필수
                if (StringUtil.isNull(name) ) {
                    log.info("[{}] {} attribute is null, check scenario", fieldNode.getNodeName(), AttrName.NAME.getValue());
                    continue;
                }

                // value exec 둘 중 하나는 있는지 체크
                if (StringUtil.isNull(value) && StringUtil.isNull(exec)) {
                    log.info("[{}] {}, {} attribute is null, check scenario", fieldNode.getNodeName(), AttrName.VALUE.getValue(), AttrName.EXEC.getValue());
                    continue;
                }

                FieldType type = FieldType.getTypeEnum(typeStr);
                FieldInfo fieldInfo = new FieldInfo(name, type, value, exec);
                fieldInfos.add(fieldInfo);
            }
        }

        return fieldInfos;
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
