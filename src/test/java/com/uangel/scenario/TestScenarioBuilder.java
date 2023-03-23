package com.uangel.scenario;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author dajin kim
 */
public class TestScenarioBuilder {
    private final String fileDir = "./src/main/resources/scenario/";

    @Test
    public void parseMrfcScenario() throws IOException, SAXException {
        String filePath = fileDir + "mrfc_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
    }

    @Test
    public void parseMrfpScenario() throws IOException, SAXException {
        String filePath = fileDir + "mrfp_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
    }

    @Test
    public void parseMrfcHbScenario() throws IOException, SAXException {
        String filePath = fileDir + "mrfc_basic_hb.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
    }

    @Test
    public void parseMrfpHbScenario() throws IOException, SAXException {
        String filePath = fileDir + "mrfp_basic_hb.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
        System.out.println(scenario.getMsgNameList());
        System.out.println("First Not Optional Recv Phase Index : " + scenario.getFirstRecvPhaseIdx());
    }

    @Test
    public void parseMrfcNopScenario() throws IOException, SAXException {
        String filePath = fileDir + "mrfc_basic_nop.xml";
        filePath = fileDir + "amf_testvcif_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        //System.out.println(scenario);
        //System.out.println(scenario.getMsgNameList());
        System.out.println(scenario.getPhase(0));
    }

}
