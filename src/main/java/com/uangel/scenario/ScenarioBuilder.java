package com.uangel.scenario;

import com.uangel.scenario.type.AttrName;
import com.uangel.scenario.phases.*;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class ScenarioBuilder {
    public static final DocumentBuilder DOCUMENT_BUILDER = getDefaultDocumentBuilder();

    private ScenarioBuilder() {
        // nothing
    }

    public static Document parseXML(String s) throws SAXException, IOException {
        if (DOCUMENT_BUILDER == null) return null;
        synchronized (DOCUMENT_BUILDER) {
            return DOCUMENT_BUILDER.parse(new InputSource(new StringReader(s)));
        }
    }

    public static Node parseXMLSnippet(String s) throws SAXException, IOException {
        Document doc = parseXML(s);
        if (doc != null) return doc.getFirstChild();
        return null;
    }

    public static Scenario fromXMLString(String xmlString) throws IOException, SAXException {
        Document doc = parseXML(xmlString);
        if (doc != null) return ScenarioBuilder.fromXMLDocument(doc);
        return null;
    }

    // 파일 이름
    public static Scenario fromXMLFileName(String filename) throws IOException, SAXException {
        return fromXMLFileName(filename, true);
    }
    public static Scenario fromXMLFileName(String filename, boolean isProtoMode) throws SAXException, IOException {
        if (DOCUMENT_BUILDER == null) return null;

        File fXmlFile = new File(filename);
        Document doc;
        synchronized (DOCUMENT_BUILDER) {
            doc = DOCUMENT_BUILDER.parse(fXmlFile);
        }
        return fromXMLDocument(doc, isProtoMode);
    }

    public static Scenario fromXMLDocument(Document doc) {
        return fromXMLDocument(doc, true);
    }
    public static Scenario fromXMLDocument(Document doc, boolean isProtoMode) {
        Element scenarioEle = doc.getDocumentElement();
        NamedNodeMap attr = scenarioEle.getAttributes();
        Node nameAttr = attr.getNamedItem(AttrName.NAME.getValue());
        String name = "Unnamed Scenario";
        if (nameAttr != null) {
            name = nameAttr.getTextContent();
        }
        List<MsgPhase> msgPhases = new ArrayList<>();
        List<LoopPhase> loopPhases = new ArrayList<>();
        Map<String, LabelPhase> labelPhases = new HashMap<>();
        int idx = 0;
        for (Node m = scenarioEle.getFirstChild(); m != null; m = m.getNextSibling()) {
            switch (m.getNodeName()) {
                case "recv" -> msgPhases.add(new RecvPhase(m, idx++));
                case "send" -> msgPhases.add(new SendPhase(m, idx++, isProtoMode));
                case "pause" -> msgPhases.add(new PausePhase(m, idx++));
                case "nop" -> msgPhases.add(new NopPhase(m, idx++));
                case "loop" -> loopPhases.add(new LoopPhase(m, 0, isProtoMode));
                case "label" -> {
                    LabelPhase labelPhase = new LabelPhase(m, 0, isProtoMode);
                    String id = labelPhase.getId();
                    labelPhases.put(id, labelPhase);
                }
                default -> {
                    // nothing
                }
            }
        }
        Scenario scenario = new Scenario(name, msgPhases);
        scenario.setLoopPhases(loopPhases);
        scenario.setLabelPhaseMap(labelPhases);
        return scenario;
    }

    private static DocumentBuilder getDefaultDocumentBuilder() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbFactory.setValidating(true);
            dbFactory.setNamespaceAware(true);
            dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            dbFactory.setFeature("http://xml.org/sax/features/validation", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setIgnoringElementContentWhitespace(true);
            dbFactory.setIgnoringComments(true);
            dbFactory.setCoalescing(true);
            return dbFactory.newDocumentBuilder();
        } catch (Exception e) {
            log.warn("Fail to build DocumentBuilder", e);
            return null;
        }
    }

}
