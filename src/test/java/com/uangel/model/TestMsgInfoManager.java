package com.uangel.model;

import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author dajin kim
 */
@Slf4j
public class TestMsgInfoManager {

    @Test
    public void testMsgInfoManager() throws IOException, SAXException {
        String filePath = "./src/main/resources/scenario/mrfc_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        log.debug("{}", scenario);

        // MsgInfo
        log.debug("{}", scenario.getMsgNameList());
    }
}
