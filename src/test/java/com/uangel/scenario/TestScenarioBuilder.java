package com.uangel.scenario;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author dajin kim
 */
public class TestScenarioBuilder {

    @Test
    public void parseMrfcScenario() throws IOException, SAXException {
        String filePath = "./src/main/resources/scenario/mrfc_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
    }

    @Test
    public void parseMrfpScenario() throws IOException, SAXException {
        String filePath = "./src/main/resources/scenario/mrfp_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
    }

    @Test
    public void parseMrfcHbScenario() throws IOException, SAXException {
        String filePath = "./src/main/resources/scenario/mrfc_basic_hb.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
    }

    @Test
    public void parseMrfpHbScenario() throws IOException, SAXException {
        String filePath = "./src/main/resources/scenario/mrfp_basic_hb.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
        System.out.println(scenario.getMsgNameList());
        System.out.println("First Not Optional Recv Phase Index : " + scenario.getFirstRecvPhaseIdx());
    }

}
