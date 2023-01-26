package com.uangel.model;

import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import com.uangel.service.AppInstance;
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

        AppInstance instance = AppInstance.getInstance();
        instance.setScenario(scenario);

        // MsgInfoManager
        MsgInfoManager msgInfoManager = MsgInfoManager.getInstance();
        msgInfoManager.initList();
        log.debug("{}", msgInfoManager.getMsgNameList());
    }
}
