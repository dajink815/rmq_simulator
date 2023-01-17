package com.uangel.util;

import org.w3c.dom.Node;

/**
 *
 * @author kangmoo Heo
 */
public class XmlUtil {

    private XmlUtil() {
        // nothing
    }

    public static String getStrParam(Node node) {
        return node == null ? null : node.getTextContent();
    }

    public static Boolean getBoolParam(Node node) {
        return node == null ? null : node.getTextContent().equalsIgnoreCase("true");
    }

    public static Integer getIntParam(Node node) {
        return node == null ? null : Integer.parseInt(node.getTextContent());
    }

    public static Float getFloatParam(Node node) {
        return node == null ? null : Float.parseFloat(node.getTextContent());
    }

    public static String getStrParamWithDefault(Node node, String defaultVal) {
        return node == null ? defaultVal : node.getTextContent();
    }

    public static Boolean getBoolParamWithDefault(Node node, boolean defaultVal) {
        return node == null ? defaultVal : node.getTextContent().equalsIgnoreCase("true");
    }

    public static Integer getIntParamWithDefault(Node node, int defaultVal) {
        return node == null ? defaultVal : Integer.parseInt(node.getTextContent());
    }

    public static Float getFloatParamWithDefault(Node node, float defaultVal) {
        return node == null ? defaultVal : Float.parseFloat(node.getTextContent());
    }
}
