package com.uangel.scenario;

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
import java.util.List;


@Slf4j
public class ScenarioBuilder {
    public static final DocumentBuilder DOCUMENT_BUILDER = getDefaultDocumentBuilder();

    private ScenarioBuilder() {
        // nothing
    }

    public static Node parseXMLSnippet(String s) throws SAXException, IOException {
        Document doc = parseXML(s);
        if (doc != null) return doc.getFirstChild();
        return null;
    }

    public static Document parseXML(String s) throws SAXException, IOException {
        if (DOCUMENT_BUILDER == null) return null;
        synchronized (DOCUMENT_BUILDER) {
            return DOCUMENT_BUILDER.parse(new InputSource(new StringReader(s)));
        }
    }

    public static Scenario fromXMLString(String xmlString) throws IOException, SAXException {
        Document doc = parseXML(xmlString);
        if (doc != null) return ScenarioBuilder.fromXMLDocument(doc);
        return null;
    }

    // 파일 이름
    public static Scenario fromXMLFilename(String filename) throws SAXException, IOException {
        if (DOCUMENT_BUILDER == null) return null;

        File fXmlFile = new File(filename);
        Document doc;
        synchronized (DOCUMENT_BUILDER) {
            doc = DOCUMENT_BUILDER.parse(fXmlFile);
        }
        return fromXMLDocument(doc);
    }

    public static Scenario fromXMLDocument(Document doc) {
        Element scenario = doc.getDocumentElement();
        NamedNodeMap attr = scenario.getAttributes();
        Node nameAttr = attr.getNamedItem("name");
        String name = "Unnamed Scenario";
        if (nameAttr != null) {
            // TODO - verify that the name doesn't contain special characters
            name = nameAttr.getTextContent();
        }
        List<MsgPhase> msgPhases = new ArrayList<>();
        int idx = 0;
        for (Node m = scenario.getFirstChild(); m != null; m = m.getNextSibling()) {
            switch (m.getNodeName()) {
                case "recv":
                    msgPhases.add(new RecvPhase(m, idx++));
                    break;
                case "send":
                    msgPhases.add(new SendPhase(m, idx++));
                    break;
                case "pause":
                    msgPhases.add(new PausePhase(m, idx++));
                    break;
                default:
                    break;
            }
        }
        return new Scenario(name, msgPhases);
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
