package com.uangel.scenario;

import com.uangel.scenario.phases.RecvPhase;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author dajin kim
 */
public class TestScenarioBuilder {
    private final String fileDir = "./src/test/resources/scenario/";

    @Test
    public void parseMrfcScenario() throws IOException, SAXException {
        String filePath = fileDir + "mrf/mrfc_basic.xml";
        filePath = fileDir + "pbx/awf_inonly.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);

        int firstRcvIdx = scenario.getFirstRecvPhaseIdx();
        System.out.println("First RcvIdx : " + firstRcvIdx);
        if (firstRcvIdx >= 0) {
            RecvPhase firstRcv = (RecvPhase) scenario.getPhase(firstRcvIdx);
            System.out.println(firstRcv);
            System.out.println(firstRcv.getMsgName());
        }
    }

    @Test
    public void parseMrfpScenario() throws IOException, SAXException {
        String filePath = fileDir + "mrf/mrfp_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
    }

    @Test
    public void parseMrfcHbScenario() throws IOException, SAXException {
        String filePath = fileDir + "mrf/mrfc_basic_hb.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
    }

    @Test
    public void parseMrfpHbScenario() throws IOException, SAXException {
        String filePath = fileDir + "mrf/mrfp_basic_hb.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
        System.out.println(scenario.getMsgNameList());
        System.out.println("First Not Optional Recv Phase Index : " + scenario.getFirstRecvPhaseIdx());
    }

    @Test
    public void parseMrfcNopScenario() throws IOException, SAXException {
        String filePath = fileDir + "mrf/mrfc_basic_nop.xml";
        filePath = fileDir + "amf_testvcif_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        //System.out.println(scenario);
        //System.out.println(scenario.getMsgNameList());
        System.out.println(scenario.getPhase(0));
    }

}
