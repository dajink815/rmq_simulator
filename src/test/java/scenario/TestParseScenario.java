package scenario;

import com.uangel.ScenarioRunner;
import com.uangel.model.MsgInfoManager;
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
public class TestParseScenario {

    @Test
    public void parseScenario() throws IOException, SAXException {
        String filePath = "./src/main/resources/scenario/mrfc_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        System.out.println(scenario);
    }

    @Test
    public void testScenarioRunner() {
        String localIp = "127.0.0.1";
        String[] args = {"-sf", "./src/main/resources/scenario/mrfc_basic.xml",
                "-t", "proto", "-pf", "./src/main/resources/proto/mrfp-external-msg-1.0.3.jar",
                "-rl", "local_queue", "-rh", localIp,
                "-rp", "5672", "-m", "1"};

        new ScenarioRunner().run(args);
    }

    @Test
    public void testHelpCommandInfo() {
        String[] args = {"-h"};
        new ScenarioRunner().run(args);
    }

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
